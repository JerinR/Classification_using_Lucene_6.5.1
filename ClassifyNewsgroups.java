package com.lucene.classification;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.*;
import org.apache.lucene.document.Field.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.store.*;



public class ClassifyNewsgroups {
	
	
	public static final String[] NEWSGROUPS={
		"alt.atheism",
		"comp.graphics",
		"comp.os.ms-windows.misc",
		"comp.sys.ibm.pc.hardware",
		"comp.sys.mac.hardware",
		"comp.windows.x",
		"misc.forsale",
		"rec.autos",
		"rec.motorcycles",
		"rec.sport.baseball",
		"rec.sport.hockey",
		"sci.crypt",
		"sci.electronics",
		"sci.med",
		"sci.space",
		"soc.religion.christian",
		"talk.politics.guns",
		"talk.politics.mideast",
		"talk.politics.misc",
		"talk.religion.misc"
	};
	
	private Analyzer mAnalyzer;
	public static final String ENCODING = "UTF-8";
	private static int[][] confusionMatrix;
	
	
	public static void main(String[] args) throws CorruptIndexException, LockObtainFailedException, IOException
	{
        File trainDir = new File("E:\\Lucene\\20news-bydate\\20news-bydate-train");
        Path indexDir = Paths.get("E:\\Lucene\\ClassifyResult");
        File testDir = new File("E:\\Lucene\\20news-bydate\\20news-bydate-test");
        ClassifyNewsgroups classifier 
            = new ClassifyNewsgroups("std");
        float total = 0;
        float truePositives = 0;
       
        if (Files.exists(indexDir,LinkOption.NOFOLLOW_LINKS)) {
            classifier.buildIndex(indexDir, trainDir);
        }
        classifier.testIndex(indexDir, testDir);
	}
	
	
	public ClassifyNewsgroups(String aType){
		if("std".equals(aType)){
			mAnalyzer = new StandardAnalyzer();
		}
		else if("lc".equals(aType)){
			mAnalyzer = new Analyzer(){
				@Override
				protected TokenStreamComponents createComponents(String fieldName){
					Tokenizer source = new StandardTokenizer();
					TokenStream filter = new LowerCaseFilter(source);
					return new TokenStreamComponents(source,filter);
				}
			};
		}
		else if("ngram".equals(aType)){
			mAnalyzer = new Analyzer(){
				@Override
				protected TokenStreamComponents createComponents(String fieldName){
					Tokenizer source = new NGramTokenizer(4,4);
					return new TokenStreamComponents(source);
				}
			};
		}
	}

	/***
	 * creates indexes for the file using IndexWriterConfig and IndexWriter
	 * @param indexDir
	 * @param dataDir
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public void buildIndex(Path indexDir, File dataDir)
			throws IOException, FileNotFoundException{
		Directory fsDir = FSDirectory.open(indexDir);
	    IndexWriterConfig iwConf 
	        = new IndexWriterConfig(mAnalyzer);
	    iwConf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
	    IndexWriter indexWriter
	        = new IndexWriter(fsDir,iwConf);
	    
	    File[] groupsDir = dataDir.listFiles();
	    for (File group : groupsDir) {
	        String groupName = group.getName();
	        File[] posts = group.listFiles();
	        for (File postFile : posts) {
	            String number = postFile.getName();
	            NewsPost post = parse(postFile, groupName, number);
	            Document d = new Document();
	            d.add(new StringField("category",
	                                  post.group(),Store.YES));
	            d.add(new TextField("text",
	                                post.subject(),Store.NO));
	            d.add(new TextField("text",
	                                post.body(),Store.NO));
	            indexWriter.addDocument(d);
	        }
	    }
	    indexWriter.commit();	    
	    indexWriter.close();
	}
	
	/***
	 * this method extracts the subject of and the body from the file 
	 * @param inFile
	 * @param newsgroup
	 * @param number
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	NewsPost parse(File inFile, String newsgroup, String number)
	    throws IOException, FileNotFoundException {
	    FileReader reader = new FileReader(inFile);
	    BufferedReader br = new BufferedReader(reader);
	    String currentLine = null;
	    StringBuilder body = new StringBuilder();
	    String subject = null;
	    Boolean flag = false;
	    while((currentLine=br.readLine())!=null){
	    	if(currentLine.length()==0)
	    		break;
	    	if(flag){
           	 	body.append(currentLine);
            }
	    	else{
		    	 String[] words = currentLine.split(" ");
	             for (String word : words) {
	                 if (word.equals("Subject:")){
	                	 subject = currentLine.substring(word.length());
	                	 flag = true;
	                	 break;
	                 }
	                 else break;
	             }
	    	}
             
	    }
	    return new NewsPost(newsgroup, number, subject, body.toString());	   
	}

	/***
	 * this method uses the indexed files and searches for the best fit for a test file
	 * and creates the confusion matrix
	 * @param indexDir
	 * @param testDir
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private void testIndex(Path indexDir, File testDir) 
		throws IOException, FileNotFoundException{
		Directory fsDir = FSDirectory.open(indexDir);
		IndexReader reader = DirectoryReader.open(fsDir);
		IndexSearcher searcher = new IndexSearcher(reader);
		confusionMatrix = new int[NEWSGROUPS.length][NEWSGROUPS.length];
		File[] groupDir = testDir.listFiles();
		for(File group : groupDir){
			int postCt = 0;
			String groupName = group.getName();
			int rowIdx = Arrays.binarySearch(NEWSGROUPS, groupName);
			File[] posts = group.listFiles();
			for(File postFile : posts){
				postCt++;
				String number = postFile.getName();
				NewsPost post = parse(postFile, groupName,number);
				BooleanQuery termsQuery = buildQuery(post.subject()+" "+post.body());
				//getting the first best result
				TopDocs hits = searcher.search(termsQuery,1);
		         ScoreDoc[] scoreDocs = hits.scoreDocs;
		         Map<Integer,Integer> map= new HashMap();
		         int max = 0;
		         int index = 0;         
		         for (int n = 0; n < scoreDocs.length; ++n) {
		         	ScoreDoc sd = scoreDocs[n];
			         int docId = sd.doc;
			         Document d = searcher.doc(docId);
			         String category = d.get("category");
			         //update confusion matrix
		             int colIdx = Arrays.binarySearch(NEWSGROUPS,category);
		             confusionMatrix[rowIdx][colIdx]++; 
		         }
		        
			}
			System.out.print(groupName);
            for (int i=0; i<NEWSGROUPS.length; i++) 
                 System.out.printf("| %4d ", confusionMatrix[rowIdx][i]);
            System.out.println("|");
		}
	}
	
	/***
	 * this method builds the query using which is later passed the IndexSearcher
	 * @param text
	 * @return
	 * @throws IOException
	 */
	BooleanQuery buildQuery(String text) throws IOException{
		Builder termsQuery = new BooleanQuery.Builder();
		Reader textReader = new StringReader(text);
		TokenStream tokStream = mAnalyzer.tokenStream("text",textReader);
		try{
			tokStream.reset();
			CharTermAttribute terms = tokStream.addAttribute(CharTermAttribute.class);
			int ct = 0;
			while(tokStream.incrementToken() && ct++ < 1024){
				termsQuery.add(new TermQuery(new Term("text", terms.toString())),Occur.SHOULD);
			}
			tokStream.end();
		}finally{
			tokStream.close();
			textReader.close();
		}
		return termsQuery.build();
	}
}
