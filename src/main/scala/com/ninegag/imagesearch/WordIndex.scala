package com.ninegag.imagesearch

import java.io.FileInputStream

import org.bytedeco.javacpp.indexer.UByteIndexer
import org.bytedeco.javacpp.opencv_core.{CV_8U, Mat}
import org.bytedeco.javacpp.opencv_flann
import org.bytedeco.javacpp.opencv_flann._


object WordIndex {

    val words = new Mat(0, 32, CV_8U); // The matrix that stores the visual words.

    var kdIndex: Index = null

    def init(visualWordsPath: String) = {

        if (!readVisualWords(visualWordsPath)) {
            System.exit(1)
        }

        println("Building the word index. (n=%d)".format(words.rows))

        //val m_features: Mat = new Mat(words.ptr(0))
        //cvflann :: Matrix < unsigned char > m_features
//            ((unsigned char *) words -> ptr < unsigned char > (0), words -> rows, words -> cols);

        //val kdIndex = new HierarchicalClusteringIndex[Hamming](m_features, HierarchicalClusteringIndexParams(10, opencv_flann::FLANN_CENTERS_RANDOM, 8, 100));
        //kdIndex = new Index(words, new HierarchicalClusteringIndexParams(10, opencv_flann.FLANN_CENTERS_RANDOM, 8, 100), opencv_flann.FLANN_DIST_HAMMING)
        kdIndex = new Index(words, new LshIndexParams(12, 20, 2), opencv_flann.FLANN_DIST_HAMMING)
        //kdIndex = new Index(words, new LinearIndexParams(), opencv_flann.FLANN_DIST_HAMMING)
        //this.kdIndex = new Index(words, new SavedIndexParams("/home/csy/tmp/kdIndex.dat"), opencv_flann.FLANN_DIST_HAMMING)
        println("WordIndex ready")
    }

    def readVisualWords(fileName: String): Boolean = {

        println("Reading the visual words file.")

        try {
            // Open the input file.
            //ifstream ifs;
            val ifs = new FileInputStream(fileName)
            //ifs.open(fileName.c_str(), ios_base::binary);
            //if (!ifs.good()) {
            //  cout << "Could not open the input file." << endl;
            //  return false;
            //}

            var x = 0
            //unsigned char c
            var c: Int = 0
            while (ifs.available() > 0) {
                val line = new Mat(1, 32, CV_8U)
                val indexer = line.createIndexer().asInstanceOf[UByteIndexer]
                for (i_col <- 0 until 32) {
                    c = ifs.read()
                    indexer.put(0, i_col, c)
                    if (x<100) {
                        x=x+1
                        print(s"$c ")
                    }
                    //line.col()at(0, i_col) = c
                }
                words.push_back(line)
            }

            ifs.close()

            true

        } catch {
            case e: Exception =>
                println("Got exception :%s".format(e.getMessage))
                e.printStackTrace()
                false
        }
    }

    def knnSearch(query: Mat, indices: Mat, dists: Mat, knn: Int) = {
        //cvflann::KNNResultSet<int> m_indices(knn);
        //m_indices.init(indices.data(), dists.data());

        kdIndex.knnSearch(query, indices, dists, knn, new SearchParams(200))
    }


}
