package com.thoughtworks.vodqa.Controller

import com.thoughtworks.vodqa.Controller.User
import play.api.libs.json._
import play.api.cache.Cache
import scala.concurrent.duration._
import collection.mutable.Seq

import scala.collection.immutable.HashSet


case class User (val userId: Option[Int],val userName: String,val employer: String,val location: String)

object User{
  implicit val userFormat = Json.format[User]
}
import play.api.mvc.{Action, Controller}

object VodQAController extends Controller{

  var listOfUsers = Map[Int,User](
   1-> User(Some(1),"Robert","facebook","California"),
  2->User(Some(2) ," Aaron","Twitter","Irvine"),
  3->User(Some(3) ," Michael","Google","New York"),
  4 -> User(Some(4), " Dan", "Yahoo", "Atlanta"),
  5->User(Some(5) ," Steve","ThoughtWorks","Chicago"))

  var count: Int = 5



  def listUsers() = Action{
    request=>Ok(Json.toJson(listOfUsers.values)).withHeaders("Content-Type"->"application/json")
  }

  def getUser(userId: Int) = Action{
    request => Ok(Json toJson(listOfUsers get(userId))).withHeaders("Content-Type"->"application/json")
  }

  def updateUser(id: Int) = Action(parse.json){
    request => request.body.validate[User].map {
      case user: User =>
        val newUser = user.copy(userId = Some(id))
        listOfUsers -= id
        listOfUsers += id->newUser
        Ok(Json.toJson(newUser)).withHeaders("Content-Type"->"application/json")
    }.recoverTotal(e=>BadRequest(s"$e"))
  }

  def putUser() = Action(parse.json){
    request=>request.body.validate[User].map {
      case user =>
        count = count + 1
        val newUser = user.copy(userId = Some(count))
        listOfUsers += (count -> newUser)
        Ok(Json.toJson(newUser)).withHeaders("Content-Type"->"application/json")
    }.recoverTotal(e=>BadRequest(s"$e"))
  }

  def addUsers() = Action(parse.json){
    request => request.body.validate[Seq[User]].map {
      case users=> users.map {
        user =>
          count=count+1
          listOfUsers+=(count->user.copy(userId=Some(count)))
      }
        Ok(Json.toJson(listOfUsers.values)).withHeaders("Content-Type"->"application/json")
    }.recoverTotal(e=>BadRequest(s"$e"))

  }

  def deleteUser(id: Int) = Action{
    request=>
      listOfUsers-=id
      Ok("")
  }
}
