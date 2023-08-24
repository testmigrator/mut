import sys


def generateWordVec(corpusFilepath: str, modelFilepath: str):
    from gensim.models import word2vec
    sentences = word2vec.LineSentence(corpusFilepath)
    model = word2vec.Word2Vec(sentences, vector_size=128, window=2, min_count=0, sg=1)
    model.save(modelFilepath)


if __name__ == '__main__':
    generateWordVec(sys.argv[1], sys.argv[2])
    # generateWordVec("/Users/gaoyi/IdeaProjects/testSoftware/doc/word2vec/corpus.txt",
    #                 "/Users/gaoyi/IdeaProjects/testSoftware/doc/word2vec/word2vec.model")
