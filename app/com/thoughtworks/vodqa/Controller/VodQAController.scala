package com.thoughtworks.vodqa.Controller

import play.api.libs.json._

case class Location(state: String,city: String)
case class User (val userId: Option[Int],val userName: String,val employer: String,val location: Location)

object User{
  implicit val locationFormat = Json.format[Location]
  implicit val userFormat = Json.format[User]
}
import play.api.mvc.{Action, Controller}

object VodQAController extends Controller{

  var listOfUsers = Map[Int,User](
   1-> User(Some(1),"Robert","facebook",Location("California","San Jose")),
  2->User(Some(2) ,"Aaron","Twitter",Location("New York","New York")),
  3->User(Some(3) ,"Michael","Google",Location("California","Palo Alto")),
  4 -> User(Some(4), "Dan", "Yahoo", Location("California","Mountain View")),
  5->User(Some(5) ,"Steve","ThoughtWorks",Location("California","San Francisco")))

  var count: Int = 5


  def listUsers(employer: Option[String],location: Option[String]) = Action{
    request=>
      val filteredUsers = listOfUsers.values.filter { user =>
        employer
          .map(_.equals(user.employer))
          .getOrElse(true) &&
          location
            .map(_.equals(user.location))
            .getOrElse(true)
      }
      Ok(Json.toJson(filteredUsers.toList.sortBy(_.userId))).withHeaders("Content-Type"->"application/json")
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
        val newUser = user copy(userId = Some(count))
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
