package com.ninegag.imagesearch

import javax.swing.WindowConstants

import org.bytedeco.javacpp.{opencv_core, opencv_features2d, opencv_imgcodecs, opencv_imgproc}
import org.bytedeco.javacpp.opencv_core.{DMatch, DMatchVector, Mat, Size}
import org.bytedeco.javacpp.opencv_features2d.{BFMatcher, DescriptorMatcher, FlannBasedMatcher}
import org.bytedeco.javacpp.opencv_imgcodecs._
import org.bytedeco.javacv.{CanvasFrame, OpenCVFrameConverter}

import scala.collection.mutable.ArrayBuffer

/**
  * Created by csy on 5/24/16.
  */
object Main extends App {

    WordIndex.init("/home/csy/Workspace/github/pastec/build/visualWordsORB.dat")

    //val input1 = imread("/home/csy/Downloads/game-of-thrones-season-6-episode-1-ss06.jpg", IMREAD_GRAYSCALE)
    //val input1 = imread("/home/csy/Downloads/Mr-Spock-mr-spock-10874060-1036-730.jpg", IMREAD_GRAYSCALE)
    //val input1 = imread("/home/csy/Downloads/fwp_1.png", IMREAD_GRAYSCALE)
    //val input2 = imread("/home/csy/Downloads/fwp_2.jpg", IMREAD_GRAYSCALE)
    //val input1 = imread("/home/csy/Downloads/brian_1.jpg", IMREAD_GRAYSCALE)
    //val input2 = imread("/home/csy/Downloads/brian_2.jpg", IMREAD_GRAYSCALE)

    val input1 = imread("/home/csy/Downloads/success-kid-1.jpg")
    val input2 = imread("/home/csy/Downloads/success-kid-2.jpg")
    //val input1 = imread("/home/csy/Downloads/wolf_1.jpg", IMREAD_GRAYSCALE)
    //val input2 = imread("/home/csy/Downloads/wolf_2.jpg", IMREAD_GRAYSCALE)

    val MAX_DIMENSION = 700
    val sz1 = new Size((input1.size().width().toDouble / input1.size().height() * MAX_DIMENSION).toInt, MAX_DIMENSION)
    val sz2 = new Size((input2.size().width().toDouble / input2.size().height() * MAX_DIMENSION).toInt, MAX_DIMENSION)

    val resized1 = new Mat()
    val resized2 = new Mat()


    opencv_imgproc.resize(input1, resized1, sz1)
    opencv_imgproc.resize(input2, resized2, sz2)

    val MEME_CAPTION_HEIGHT = 100
    val im1 = resized1.rowRange(MEME_CAPTION_HEIGHT, resized1.rows()-MEME_CAPTION_HEIGHT)
    val im2 = resized2.rowRange(MEME_CAPTION_HEIGHT, resized2.rows()-MEME_CAPTION_HEIGHT)

    val doc1 = Indexer.generateFingerprint(im1)
    val doc2 = Indexer.generateFingerprint(im2)

    val matches1 = dmatch(doc1._2, doc2._2)
    //val matches2 = bmatch(doc1._2, doc2._2)

    val im3 = new Mat()
    opencv_features2d.drawMatches(im1, doc1._3, im2, doc2._3, matches1, im3)
    display(im3, "matches")

    //hamm(doc1._1.split(' '), doc2._1.split(' '))

    println("\n--------------\n")
    println(ES.addDoc(doc1._1, "2", "bad-luck-brian"))
    println(ES.addDoc(doc2._1, "3", "bad-luck-brian"))
    println("\n--------------\n")
    //println(ES.findDoc(doc1._1).toString)
    //println(ES.findDoc(doc2._1).toString)
    //println(doc2._1)
    println("\n--------------\n")

    println("Done")

    def hamm(s: Seq[String], t:Seq[String]): Int = {
        val set1 = s.toSet
        val set2 = t.toSet

        val common = s.count(set2.contains) + t.count(set1.contains)
        //val common = set1.intersect(set2).size
        println(s"\n\nGot $common common words")

        val hamm = s.size + t.size - common
        val perc = 1-(hamm.toDouble / (s.size + t.size))
        println("Hamming Distance = %d (%.2f)\n".format(hamm, perc))

        hamm
    }

    def dmatch(dsc1: Mat, dsc2: Mat) = {
        val m = DescriptorMatcher.create("BruteForce-Hamming")
        val dm = new DMatchVector()
        m.`match`(dsc1, dsc2, dm)

        var max_dist = 0.0
        var min_dist = 100.0

        //-- Quick calculation of max and min distances between keypoints
        for(i <-  0 until dsc1.rows) {
            val dist = dm.get(i).distance
            if( dist < min_dist ) min_dist = dist
            if( dist > max_dist ) max_dist = dist
        }
        printf("-- Max dist : %f \n", max_dist )
        printf("-- Min dist : %f \n", min_dist )

        val good_matches = new ArrayBuffer[DMatch]
        for(i <- 0 until dsc1.rows) {
//            if( dm.get(i).distance <= Math.max(2*min_dist, 10)) {
            if( dm.get(i).distance <= 10) {
                good_matches.+=(dm.get(i))
            }
        }
        val perc = good_matches.size.toDouble / dsc1.rows()

        println("-- Good matches : %d, (%.2f)".format(good_matches.size, perc))

        new DMatchVector(good_matches:_*)
    }

    def bmatch(dsc1: Mat, dsc2: Mat) = {
        val b = new BFMatcher(opencv_core.NORM_HAMMING, true)
        val dm = new DMatchVector()
        b.`match`(dsc1, dsc2, dm)

        var max_dist = 0.0
        var min_dist = 100.0

        //-- Quick calculation of max and min distances between keypoints
        for(i <-  0 until dsc1.rows) {
            val dist = dm.get(i).distance
            if( dist < min_dist ) min_dist = dist
            if( dist > max_dist ) max_dist = dist
        }
        printf("-- Max dist : %f \n", max_dist )
        printf("-- Min dist : %f \n", min_dist )

        var good_matches = 0
        for(i <- 0 until dsc1.rows) {
            if( dm.get(i).distance <= Math.max(2*min_dist, 0.02)) {
                good_matches = good_matches + 1
            }
        }
        val perc = good_matches.toDouble / dsc1.rows()
        println(s"-- Good matches : $good_matches, ($perc)")

        dm
    }


    def display(image: Mat, caption: String): Unit = {
        // Create image window named "My Image."
        val canvas = new CanvasFrame(caption, 1)

        // Request closing of the application when the image window is closed.
        canvas.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)

        // Convert from OpenCV Mat to Java Buffered image for display
        val converter = new OpenCVFrameConverter.ToMat()
        // Show image on window
        canvas.showImage(converter.convert(image))
    }
}
