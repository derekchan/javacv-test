package com.ninegag.imagesearch

import org.bytedeco.javacpp.opencv_core.{KeyPointVector, Mat}

/**
  * Created by csy on 5/24/16.
  */
object Indexer {
    def generateFingerprint(im: Mat): (String, Mat, KeyPointVector) = {
        val fe = new FeatureExtractor
        fe.processImage(im)
        (fe.matchedWords
            .map(Base58.encode(_))
           .mkString(" "), fe.descriptors, fe.keyPoints)
    }
}
