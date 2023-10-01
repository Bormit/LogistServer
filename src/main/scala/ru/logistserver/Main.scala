package ru.logistserver

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import scala.io.StdIn

object Main extends App {
  implicit val system = ActorSystem(Behaviors.empty, "my-system")
  implicit val executionContext = system.executionContext

  private var mem: Double = 0.0
  private var lastResult: Double = 0.0

  //Команда
  val route = {
    pathPrefix("calculate") {
      get {
        path("hello") {
          println("Hello")
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
        } ~
        path("add") {
          println("add")
          parameters("acr1".as[Double], "acr2".as[Double]) { (acr1, acr2) =>
            val res = acr1 + acr2
            lastResult = res
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>Результат: $acr1 + $acr2 = $res</h1>"))
          }
        } ~
        path("sub") {
          println("sub")
          parameters("acr1".as[Double], "acr2".as[Double]) { (acr1, acr2) =>
            val res = acr1 - acr2
            lastResult = res
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>Результат: $acr1 - $acr2 = $res</h1>"))
          }
        } ~
        path("mul") {
          println("mul")
          parameters("acr1".as[Double], "acr2".as[Double]) { (acr1, acr2) =>
            val res = acr1 * acr2
            lastResult = res
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>Результат: $acr1 * $acr2 = $res</h1>"))
          }
        } ~
        path("div") {
          println("div")
          parameters("acr1".as[Double], "acr2".as[Double]) { (acr1, acr2) =>
            if (acr2 == 0)
              complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>Результат: Деление на ноль</h1>"))
            else {
              val res = acr1 / acr2
              lastResult = res
              complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>Результат: $acr1 / $acr2 = $res</h1>"))
            }
          }
        }
      } ~
      put {
        path("ms") {
          mem = lastResult
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>Заполнили в памяти $mem</h1>"))
        } ~
        path("mc") {
          mem = 0.0
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>Очистили память</h1>"))
        } ~
        path("m+") {
          mem = mem + lastResult
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>Добавили в память $lastResult</h1>"))
        } ~
        path("m-") {
          mem = mem - lastResult
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>Убавили из памяти $lastResult</h1>"))
        }
      } ~
      get {
        path("mr") {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>Значение из памяти $mem</h1>"))
        }
      }
    }
  }


  //Создали сервер и привязали к нему путь
  val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)

  println(s"Server now online. Please navigate to http://localhost:8080/hello\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done
}