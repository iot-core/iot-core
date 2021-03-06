# IoT Core Services [![Build status](https://api.travis-ci.org/iotbricks/iotbricks.svg)](https://travis-ci.org/iotbricks/iotbricks) 

## Installation

In order to run iotbricks device registry service, execute the following command:

    docker run -it --net=host iotbricks/device-registry-service:0.0.1-SNAPSHOT
    
By default device registry uses in-memory persistence. In order to switch to MongoDB backend, execute set `IOTBRICKS_MONGO_ENABLED`
environment variable to `true`:

    docker run -it --net=host -e IOTBRICKS_MONGO_ENABLED=true iotbricks/device-registry-service:0.0.1-SNAPSHOT
    
By default MongoDB-based device registry attempts to connect to MongoDB server at `localhost:27017`.

## AMQP API

### Common data structures    

    Device
    JSON {"deviceId": string, "created": string, "updated": string, "type": string, "properties": map}

### Operations

    Device create operation
    Description: Creates new instance of a device in a device registry. If "deviceId" is not specified, it will be 
                 autogenerated on the server side. 
    Address: device
    Subject: create
    Payload: Device
    Return value: ID of a device.
    
    Device update operation
    Description: Updates instance of a device in a device registry. "deviceId" field is used to identifiy target device.
    Address: device
    Subject: update
    Payload: Device

    Device save operation
    Description: Creates new device instance or updates an existing one. "deviceId" field is used to identifiy target 
                 device.  If "deviceId" is not specified, it will be autogenerated on the server side.
    Address: device
    Subject: save
    Payload: Device
    Return value: ID of a device.

    Find device by ID operation
    Description: Finds device in a device registry by "deviceId" field.
    Address: device
    Subject: findById
    Payload: deviceId string
    Return value: Device