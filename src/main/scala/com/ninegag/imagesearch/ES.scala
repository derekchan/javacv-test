package com.ninegag.imagesearch

import com.sumologic.elasticsearch.restlastic.dsl.Dsl
import com.sumologic.elasticsearch.restlastic.{Endpoint, RestlasticSearchClient, StaticEndpoint}

import scala.concurrent.Await
import scala.concurrent.duration._
import spray.json._
import DefaultJsonProtocol._
import spray.http.Uri.Query
import spray.http.{HttpMethod, HttpMethods}

object ES {


    private lazy val index = Dsl.Index("image-search")

    private lazy val typeMeme = Dsl.Type("memes")

    private lazy val es = {
        new RestlasticSearchClient(new StaticEndpoint(new Endpoint("127.0.0.1", 9201)))
    }

    def addDoc(fp: String, id: String, tags: String) = {
        val doc = Dsl.Document(
            id, Map(
                "desc" -> fp.split(' '),
                "tags" -> tags
            )
        )
        Await.result(es.index(index, typeMeme, doc), 10.seconds)
    }

    def findDoc(terms: String) = {
        //restClient.query(index, tpe, QueryRoot(WildcardQuery("f1", "case")))'

        val query = JsObject(List[JsField](
            ("query", JsObject(List[JsField](
                ("match", JsObject(List[JsField](
                    ("desc", JsObject(List[JsField](
                        ("query", JsString(terms)),
                        ("analyzer", JsString("whitespace"))
                    ):_*))
                ):_*))
            ):_*))
        ):_*).toString

        println(s"Query = $query")

        val req = es.runRawEsRequest(
            query,
            "/image-search/_search",
            HttpMethods.GET,
            Query(("_source","false"))
        )

        Await.result(req, 10.seconds)
    }
}
