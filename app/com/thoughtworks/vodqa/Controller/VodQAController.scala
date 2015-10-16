package com.thoughtworks.vodqa.Controller

import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future

case class Location(state: String,city: String)
case class User (val userId: Option[Int],val userName: Option[String],val employer: Option[String],val location: Option[Location]){
  def areDetailsPresent(): Boolean = {
    this.userName.isDefined && employer.isDefined && location.isDefined
  }

  def updateName(that: User): User = {
    if(that.userName.isDefined) copy(userName = that.userName) else this
  }

  def updateEmployer(that: User) = {
    if(that.employer.isDefined) copy(employer = that.employer) else this
  }

  def updateLocation(that: User) = {
    if(that.location.isDefined) copy(location = that.location) else this
  }
}

object User{
  implicit val locationFormat = Json.format[Location]
  implicit val userFormat = Json.format[User]
}
import play.api.mvc.{Action, Controller}

object VodQAController extends Controller{

  var listOfUsers = Map[Int,User](
   1-> User(Some(1),Some("Robert"),Some("facebook"),Some(Location("California","San Jose"))),
  2->User(Some(2) ,Some("Aaron"),Some("Twitter"),Some(Location("New York","New York"))),
  3->User(Some(3) ,Some("Michael"),Some("Google"),Some(Location("California","Palo Alto"))),
  4 -> User(Some(4), Some("Dan"),Some("Yahoo"), Some(Location("California","Mountain View"))),
  5->User(Some(5) ,Some("Steve"),Some("ThoughtWorks"),Some(Location("California","San Francisco"))))

  var count: Int = 5


  def listUsers(employer: Option[String],location: Option[String]) = Action.async{
    request=>
      val filteredUsers = listOfUsers.values.filter { user =>
        employer
          .map(_.equals(user.employer))
          .getOrElse(true) &&
          location
            .map(_.equals(user.location))
            .getOrElse(true)
      }
      Future{Ok(Json.toJson(filteredUsers.toList.sortBy(_.userId))).withHeaders("Content-Type"->"application/json")}
  }

  def getUser(userId: Int) = Action.async{
    request =>
      Future{Ok(Json toJson(listOfUsers get(userId))).withHeaders("Content-Type"->"application/json")}
  }

  def updateUser(id: Int) = Action.async(parse.json){
    request => request.body.validate[User].map {
      case user: User =>
        val oldUser: User = listOfUsers(id)
        val newUser: User = (oldUser updateName(user) updateEmployer(user) updateLocation(user))
        listOfUsers -= id
        listOfUsers += id->newUser
        Future{Ok(Json.toJson(newUser)).withHeaders("Content-Type"->"application/json")}
    }.recoverTotal(e=>Future{BadRequest(s"$e")})
  }

  def putUser() = Action.async(parse.json){
    request=>request.body.validate[User].map {
      case user =>
        if(!user.areDetailsPresent()) throw new Exception("Not all user details are present")
        count = count + 1
        val newUser = user copy(userId = Some(count))
        listOfUsers += (count -> newUser)
        Future{Ok(Json.toJson(newUser)).withHeaders("Content-Type"->"application/json")}
    }.recoverTotal(e=>Future{BadRequest(s"$e")})
  }

  def addUsers() = Action.async(parse.json){
    request => request.body.validate[Seq[User]].map {
      case users=> users.map {
        user =>
          if(!user.areDetailsPresent()) throw new Exception("Not all user details are present")
          count=count+1
          listOfUsers+=(count->user.copy(userId=Some(count)))
      }
        Future{Ok(Json.toJson(listOfUsers.values.toList.sortBy(_.userId))).withHeaders("Content-Type"->"application/json")}
    }.recoverTotal(e=>Future{BadRequest(s"$e")})

  }

  def deleteUser(id: Int) = Action.async{
    request=>
      listOfUsers-=id
      Future{Ok("")}
  }
}
