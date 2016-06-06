package com.ninegag.imagesearch

import org.bytedeco.javacpp.indexer.{IntBufferIndexer, UByteBufferIndexer}
import org.bytedeco.javacpp.opencv_core.{KeyPointVector, Mat}
import org.bytedeco.javacpp.opencv_features2d.ORB

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class FeatureExtractor {

    val matchedWords = mutable.Set[Int]()

    //val imageHits = ArrayBuffer[HitForward]()

    var descriptors: Mat = new Mat

    var keyPoints = new KeyPointVector()

    def processImage(img: Mat) = {

        val orb = ORB.create()
        orb.setMaxFeatures(2000)
        orb.setScaleFactor(1.02)
        orb.setNLevels(100)
        //orb.setEdgeThreshold(15)
        //orb.setPatchSize(15)
        orb.detect(img, keyPoints)
        orb.compute(img, keyPoints, descriptors)

        val i_nbFeaturesExtracted = keyPoints.size().toInt
        println(s"Extracted $i_nbFeaturesExtracted features")

        var i_nbKeyPoints = 0
        var maxDist = -1
        var minDist = 101

        for (i <- 0 until i_nbFeaturesExtracted) {
            i_nbKeyPoints = i_nbKeyPoints + 1

            // Recording the angle on 16 bits.
            //val angle = keyPoints.get(i).angle / 360 * (1 << 16)
            //val x = keyPoints.get(i).pt.x
            //val y = keyPoints.get(i).pt.y

            //vector<int> indices(1);
            //vector<int> dists(1);
            val indices = new Mat(1)
            val dists = new Mat(1)

            WordIndex.knnSearch(descriptors.row(i), indices, dists, 1)

            for (j <- 0 until indices.rows) {
                val idx: IntBufferIndexer = indices.createIndexer()
                val i_wordId: Int = idx.get(0, j)

                val dist = dists.createIndexer().asInstanceOf[IntBufferIndexer].get(0, j)
                maxDist = Math.max(dist, maxDist)
                minDist = Math.min(dist, minDist)

                //printf("%d,%d\t", i_wordId, dist)

                //println(s"Matched word #$i_wordId")
                //if (matchedWords.find(i_wordId) == matchedWords.end()) {
                val DIST_THRESHOLD = 90
                //print(s"$dist ")
                if (dist < DIST_THRESHOLD) {// && !matchedWords.contains(i_wordId)) {
                    //val newHit = new HitForward(
                    //    i_wordId,
                    //    i_imageId = 0,
                    //    angle,
                    //    x,
                    //    y
                    //)
                    //println(s"$x, $y, @$angle, #$i_wordId")
                    //imageHits.+=(newHit)
                    matchedWords.add(i_wordId)
                }
            }
        }
        println("Got " + matchedWords.size + s" matched words (dist[$minDist - $maxDist])")
    }
}

