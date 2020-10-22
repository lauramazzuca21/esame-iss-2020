/* Generated by AN DISI Unibo */ 
package it.unibo.smartbell

import it.unibo.kactor.*
import alice.tuprolog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
	
class Smartbell ( name: String, scope: CoroutineScope  ) : ActorBasicFsm( name, scope ){

	override fun getInitialState() : String{
		return "s0"
	}
	@kotlinx.coroutines.ObsoleteCoroutinesApi
	@kotlinx.coroutines.ExperimentalCoroutinesApi			
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		 
		 	var Temp = 0
		 	var CID = 0 
		 	val sJson = json.SmartBellJson()
		 	var T = 0
		 	var id = 0
		return { //this:ActionBasciFsm
				state("s0") { //this:State
					action { //it:State
						discardMessages = false
						println("SmartBell | INIT")
					}
					 transition( edgeName="goto",targetState="waitForClient", cond=doswitch() )
				}	 
				state("waitForClient") { //this:State
					action { //it:State
						println("  SmartBell | Wait Client  ")
						
									sJson.reset()
						updateResourceRep(sJson.toJson() 
						)
					}
					 transition(edgeName="t033",targetState="checkTemp",cond=whenRequest("ringBell"))
				}	 
				state("checkTemp") { //this:State
					action { //it:State
						if( checkMsgContent( Term.createTerm("ringBell(TEMP)"), Term.createTerm("ringBell(T)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								 T = payloadArg(0).toInt() 
								if(  T<34 || T>38  
								 ){Temp = (36..38).random() 
								}
								else
								 {Temp = T 
								 }
									
												sJson.setBusy(true)
												sJson.setClientArrived(true)
												
								updateResourceRep(sJson.toJson() 
								)
								println("  SmartBell | Check Temp $Temp ")
						}
					}
					 transition( edgeName="goto",targetState="handleEventTempOK", cond=doswitchGuarded({ Temp < 37.5 && Temp >= 35.5  
					}) )
					transition( edgeName="goto",targetState="handleEventTempKO", cond=doswitchGuarded({! ( Temp < 37.5 && Temp >= 35.5  
					) }) )
				}	 
				state("handleEventTempKO") { //this:State
					action { //it:State
						println("  SmartBell | Temp is over  ")
						println("  SmartBell | Client Discard  ")
						answer("ringBell", "tempStatus", "tempStatus(0,$CID)"   )  
						
									sJson.setBusy(true)
									sJson.setClientArrived(false)
									sJson.setClientDenied(true)
						updateResourceRep(sJson.toJson() 
						)
					}
					 transition( edgeName="goto",targetState="waitForClient", cond=doswitch() )
				}	 
				state("handleEventTempOK") { //this:State
					action { //it:State
						println("  SmartBell | Temp is OK  ")
						println("  SmartBell | Client Accepted  ")
						 	
									id++;
									CID = id; 
									sJson.setClientArrived(false)
									sJson.setClientAccepted(true)
						updateResourceRep(sJson.toJson() 
						)
						answer("ringBell", "tempStatus", "tempStatus(1,$CID)"   )  
						forward("clientID", "clientID($CID)" ,"waiter" ) 
					}
					 transition( edgeName="goto",targetState="waitForClient", cond=doswitch() )
				}	 
			}
		}
}
