/* Generated by AN DISI Unibo */ 
package it.unibo.basicrobot

import it.unibo.kactor.*
import alice.tuprolog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
	
class Basicrobot ( name: String, scope: CoroutineScope  ) : ActorBasicFsm( name, scope ){

	override fun getInitialState() : String{
		return "s0"
	}
	@kotlinx.coroutines.ObsoleteCoroutinesApi
	@kotlinx.coroutines.ExperimentalCoroutinesApi			
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		
		  var StepTime      = 0L
		  var StartTime     = 0L    
		  var Duration      = 0L   
		  
		  var Distance		= ""
		  var Obstacle		= "" 
		return { //this:ActionBasciFsm
				state("s0") { //this:State
					action { //it:State
						println("basicrobot | START")
						unibo.robot.robotSupport.create(myself ,"basicrobotConfig.json" )
						println("basicrobot | attempts to activate the sonar pipe")
						  //For real robots
						 			var robotsonar = context!!.hasActor("robotsonar")  
						 			if( robotsonar != null ){ 
						 				println("basicrobot | WORKING WITH SONARS") 
						 				//ACTIVATE THE DATA SOURCE robotsonar
						 				forward("sonarstart", "sonarstart(1)" ,"robotsonar" ) 				
						 				//SET THE PIPE
						 				robotsonar.
						 				subscribeLocalActor("datacleaner").
						 				subscribeLocalActor("distancefilter").
						 				subscribeLocalActor("basicrobot")		//in order to perceive obstacle
						 			}else{
						 				println("basicrobot | WARNING: robotsonar NOT FOUND")
						 			}
						unibo.robot.robotSupport.move( "l"  )
						unibo.robot.robotSupport.move( "r"  )
						updateResourceRep( "stopped"  
						)
						discardMessages = false
					}
					 transition( edgeName="goto",targetState="work", cond=doswitch() )
				}	 
				state("work") { //this:State
					action { //it:State
					}
					 transition(edgeName="t10",targetState="execcmd",cond=whenDispatch("cmd"))
					transition(edgeName="t11",targetState="doStep",cond=whenRequest("step"))
					transition(edgeName="t12",targetState="endwork",cond=whenDispatch("end"))
				}	 
				state("execcmd") { //this:State
					action { //it:State
						println("$name in ${currentState.stateName} | $currentMsg")
						if( checkMsgContent( Term.createTerm("cmd(MOVE)"), Term.createTerm("cmd(MOVE)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								unibo.robot.robotSupport.move( payloadArg(0)  )
								updateResourceRep( "movedone(${payloadArg(0)})"  
								)
						}
					}
					 transition( edgeName="goto",targetState="work", cond=doswitch() )
				}	 
				state("doStep") { //this:State
					action { //it:State
						if( checkMsgContent( Term.createTerm("step(TIME)"), Term.createTerm("step(T)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
									StepTime = payloadArg(0).toLong() 	 
								updateResourceRep( "step(${StepTime})"  
								)
								unibo.robot.robotSupport.move( "w"  )
								StartTime = getCurrentTime()
						}
						stateTimer = TimerActor("timer_doStep", 
							scope, context!!, "local_tout_basicrobot_doStep", StepTime )
					}
					 transition(edgeName="t03",targetState="stepPerhapsDone",cond=whenTimeout("local_tout_basicrobot_doStep"))   
					transition(edgeName="t04",targetState="stepFail",cond=whenEvent("obstacle"))
				}	 
				state("stepPerhapsDone") { //this:State
					action { //it:State
						unibo.robot.robotSupport.move( "h"  )
						if( checkMsgContent( Term.createTerm("sonar(DISTANCE,NAME)"), Term.createTerm("sonar(D,T)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								 
												Distance = payloadArg(0)
												Obstacle = payloadArg(1)
								println("basicrobot | sonar found obstacle $Obstacle at distance $Distance")
						}
						stateTimer = TimerActor("timer_stepPerhapsDone", 
							scope, context!!, "local_tout_basicrobot_stepPerhapsDone", StepTime )
					}
					 transition(edgeName="t05",targetState="stepDone",cond=whenTimeout("local_tout_basicrobot_stepPerhapsDone"))   
					transition(edgeName="t06",targetState="stepFailDetected",cond=whenEvent("obstacle"))
				}	 
				state("stepDone") { //this:State
					action { //it:State
						unibo.robot.robotSupport.move( "h"  )
						updateResourceRep( "stepDone($StepTime)"  
						)
						answer("step", "stepdone", "stepdone(ok)"   )  
					}
					 transition( edgeName="goto",targetState="work", cond=doswitch() )
				}	 
				state("stepFailDetected") { //this:State
					action { //it:State
						println("basicrobot | stepFailDetected state ")
						if( checkMsgContent( Term.createTerm("obstacle(ARG1,ARG2)"), Term.createTerm("obstacle(DIST,OBJ)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								 Obstacle = payloadArg(1)  
								println("basicrobot | stepFailDetected for obstacle $Obstacle near end of step ")
						}
						answer("step", "stepfail", "stepfail($StepTime,$Obstacle)"   )  
					}
					 transition( edgeName="goto",targetState="work", cond=doswitch() )
				}	 
				state("stepFail") { //this:State
					action { //it:State
						println("basicrobot | stepFail state ")
						Duration = getDuration(StartTime)
						updateResourceRep( "stepFail($Duration)"  
						)
						if( checkMsgContent( Term.createTerm("obstacle(ARG1,ARG2)"), Term.createTerm("obstacle(DIST,OBJ)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								 
												Distance = payloadArg(0)
												Obstacle = payloadArg(1) 
								println("basicrobot | stepFail for obstacle $Obstacle near end of step ")
						}
						println("basicrobot | stepFail emitting obstacle($Obstacle, $Distance)")
						emit("obstacle", "obstacle($Obstacle,$Distance)" ) 
						answer("step", "stepfail", "stepfail($Duration,$Obstacle)"   )  
					}
					 transition( edgeName="goto",targetState="work", cond=doswitch() )
				}	 
				state("endwork") { //this:State
					action { //it:State
						if( checkMsgContent( Term.createTerm("end(ARG)"), Term.createTerm("end(V)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								println("basicrobot | endwork")
								updateResourceRep( "move(end)"  
								)
								utils.virtualRobotSupportQak.terminatevr(  )
						}
						emit("endall", "endall(normal)" ) 
						terminate(1)
					}
				}	 
			}
		}
}
