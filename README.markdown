# Rule Cleaner app for OpenDaylight and Falcon

*The architecture and workflow:*
![architecture][1]

1. Module: securestate, statemonitor, rulechecker, cleanup, cleanuphandler
2. Method: probeTesting

*Build the controller and run the **karaf** with topology*

*The probe test time is the switch number (seconds)*

*You don't need to do any extra things or install any features.*


#### Example: 

1. set-secure-state
```
[POST] http://127.0.0.1:8181/restconf/operations/secure-state:set-secure-state
{
    "input": {
        "level": "level-2"
    }
}
```
2. rule-cleanup
```
[POST] http://127.0.0.1:8181/restconf/operations/cleanup:rule-cleanup
```

[1]: workflow.png