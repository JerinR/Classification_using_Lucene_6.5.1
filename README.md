# Classification_using_Lucene_6.5.1
The repository contains the class(es) written for classification of 20 News Groups, modified to be compatible with Lucene 6.5.1


# Apache Lucene
Lucene is basically used for full text indexing or searching purposes.
The book which I referred was 'Text Processing in JAVA by Mitzi Morris'.

ClassifyNewGroups.java class is for the classification of 20NewsGroups.
The data set can be downloaded from http://qwone.com/~jason/20Newsgroups/
For classification purpose '20news-bydate.tar.gz' was used as it was already divided into test and training set.

The original version of the code can be found at
https://github.com/colloquial/javabook/tree/master/src/applucene/src/com/colloquial/applucene
I have hard coded the path of the train and test directories and the location where the index has to be created.
The original code uses xml for specifying the path. The xml can be found at the aforementioned link.

The confusion matrix looks like below.

News Groups  | alt.atheism | comp.graphics | comp.os.ms-windows.misc | comp.sys.ibm.pc.hardware | comp.sys.mac.hardware | comp.windows.x | misc.forsale | rec.autos | rec.motorcycles | rec.sport.baseball | rec.sport.hockey | sci.crypt | sci.electronics | sci.med | sci.space | soc.religion.christian | talk.politics.guns | talk.politics.mideast | talk.politics.misc | talk.religion.misc
------------ | ----------- | ------------- | ----------------------- | ------------------------ | --------------------- | -------------- | ------------ | --------- | --------------- | ------------------ | ---------------- | --------- | --------------- | ------- | --------- | ---------------------- | ------------------ | --------------------- | ------------------ | ------------------
alt.atheism |  237 |    4 |    7 |    3 |    2 |    3 |    6 |    1 |    3 |    2 |    3 |    8 |    1 |    6 |    2 |    2 |    1 |    3 |    2 |   23 |
comp.graphics |    3 |  157 |   33 |   28 |   14 |   30 |   20 |   10 |    8 |   11 |   15 |   14 |   12 |   12 |    8 |    2 |    2 |    2 |    1 |    7 |
comp.os.ms-windows.misc |    8 |   34 |  138 |   29 |   19 |   19 |   12 |    6 |   10 |   15 |   11 |    9 |    9 |   20 |    8 |   11 |   10 |    4 |    8 |   14 |
comp.sys.ibm.pc.hardware |    5 |   29 |   25 |  177 |   19 |   10 |   17 |   10 |    8 |    5 |    8 |    6 |   25 |   14 |    8 |    8 |    3 |    3 |    7 |    5 |
comp.sys.mac.hardware |    5 |   20 |   27 |   25 |  171 |   10 |   19 |   14 |    7 |   13 |    6 |    2 |   14 |   15 |    6 |    6 |    8 |    5 |   11 |    1 |
comp.windows.x |    3 |   32 |   37 |   15 |   17 |  196 |    9 |    7 |   11 |    6 |    8 |    6 |   12 |    8 |    9 |    7 |    3 |    5 |    2 |    2 |
misc.forsale |    7 |   19 |   25 |   18 |   25 |   14 |  177 |   18 |   13 |    6 |    5 |    3 |   18 |    8 |    8 |    7 |    3 |    3 |   10 |    3 |
rec.autos |    4 |   17 |   18 |   21 |   15 |    8 |   20 |  186 |   20 |   12 |   14 |    1 |   17 |    4 |   10 |   12 |    5 |    1 |    7 |    4 |
rec.motorcycles |    2 |    8 |   10 |    8 |    6 |    5 |    7 |   10 |  297 |   11 |    7 |    4 |    1 |    4 |    5 |    5 |    2 |    1 |    2 |    3 |
rec.sport.baseball |    8 |   17 |    8 |    4 |    8 |   18 |   13 |   18 |   10 |  204 |   23 |    3 |   11 |   13 |   13 |    8 |    2 |    3 |   12 |    1 |
rec.sport.hockey |   13 |   10 |    8 |    8 |    9 |    4 |    7 |    8 |    3 |   22 |  262 |    3 |    5 |    7 |    7 |    6 |    3 |    4 |    7 |    3 |
sci.crypt |    3 |   16 |   19 |    2 |    4 |    7 |    7 |    7 |    8 |    5 |    2 |  283 |    1 |    9 |    7 |    1 |    7 |    2 |    3 |    3 |
sci.electronics |    4 |   26 |   15 |   20 |    9 |   14 |   12 |    9 |   12 |   20 |    6 |    5 |  188 |   10 |   13 |    2 |    7 |   11 |    7 |    3 |
sci.med |    9 |   26 |   13 |   21 |   14 |    9 |   12 |   20 |   13 |   10 |    5 |   11 |   23 |  163 |    4 |   11 |    7 |    4 |    8 |   13 |
sci.space |    5 |   16 |   14 |    6 |   11 |   10 |    6 |    8 |    4 |    5 |    3 |    4 |   14 |   10 |  264 |    5 |    3 |    3 |    2 |    1 |
soc.religion.christian |   13 |   12 |    9 |    6 |    5 |    5 |    2 |    2 |    3 |    8 |    8 |    4 |    2 |   10 |    1 |  280 |    6 |    3 |    5 |   14 |
talk.politics.guns |   10 |    7 |   16 |    3 |    2 |    8 |    8 |    7 |    3 |   14 |    3 |   10 |    3 |    5 |    6 |    3 |  245 |    1 |    4 |    6 |
talk.politics.mideast |    7 |    3 |    7 |    8 |   21 |    0 |    2 |   11 |    4 |   10 |    9 |    1 |    3 |    9 |   11 |   15 |    4 |  242 |    8 |    1 |
talk.politics.misc |    9 |    8 |   10 |    5 |   12 |    1 |    6 |   10 |    4 |    9 |    9 |    4 |    5 |    9 |    7 |    4 |   25 |    7 |  157 |    9 |
talk.religion.misc |   40 |    6 |    4 |    4 |    0 |    0 |    1 |    3 |    3 |    3 |    3 |    4 |    6 |    5 |    3 |    6 |   13 |    2 |    7 |  138 |
