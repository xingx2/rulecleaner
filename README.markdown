# Rule Cleaner app for OpenDaylight and Falcon

*The architecture and workflow:*
![architecture][1]

1. Module: securestate, statemonitor, rulechecker, cleanup, cleanuphandler
2. Method: probeTesting

*Just run the **karaf**.*

*You don't need to do any extra things or install any feature.*


####Example: 

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