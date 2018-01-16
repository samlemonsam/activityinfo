
# JSON Serialization

ActivityInfo is above all a full-featured application running in a
JavaScript VM, and so JSON is our primary and canonical serialization 
form by necessity.

This library is based on GWT's elemental JSON components, which is in 
turn based on JSON.org. 


## Usage

Presently, there are two ways to define model classes that can
be serialized to and from JavaScript Objects and to JSON strings.

### Using `@JsTypes`

The easiest way is to annnotate a Java class as a simple JavaScript object
using the `@JsType` annotation:

```.java

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final Person {
    public String name;
    public int age;
    public Project[] projects;
    public boolean available;
}


@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public final Project {
    public String code;
    public double budget;
}
```

The `@JsType` annotation instructs the GWT Compiler that all instances
of the `Person` class will be simple JavaScript object, and that the
field names should be preserved as-is.

To the serialize the object to a JSON String, you can use the 
`Json.stringify()` method. In GWT-compiled code, this will translate
simply to a call to the JavaScript `JSON.stringify()` function. Because
the GWT compiler ensures that all `Person` instances are plain 
JavaScript objects, no other work is required.

On the server, the same call to the `Json.stringify()` method will use
Java reflection to generate a JSON string.

On the client, `Person` instances can also be passed directly to 
HTML5 APIs that use the [Structured Clone Algorithm](https://developer.mozilla.org/en-US/docs/Web/API/Web_Workers_API/Structured_clone_algorithm)
to handle complex JavaScript objects, such as IndexedDB. This means
you can store and retrieve with `Person` objects, or arrays of `Person` 
objects from the IndexedDB store with zero serialization cost.

### Using the `JsonSerializable` interface

Not all model classes will have flat data structures that map nicely to 
JavaScript objects. In some cases, you may want to use Java's polymorphism
to serialize hierarchies of objects, or represent values with richer
classes than `String`s and `double`s. 

In this case, your model class can implement the `JsonSerializable`
interface to provide custom serialization and deserialization logic:

```.java
public final Person implements JsonSerializable {
    private String name;
    private int age;
    private List<Project> projects;
    
    @Override
    public JsonValue toJson() {
        JsonValue object = Json.createObject();
        object.put("name", name);
        object.put("age", age);
        
        JsonValue projectArray = Json.createArray();
        for(Project project : projects) {
            projectArray.add(project.toJson());
        }
        object.put("projects", projectArray);
        return object;
    }
    
    public static Person fromJson(JsonValue json) {
        Person person = new Person();
        person.name = json.getString("name");
        person.age = json.getInt("age");
        person.projects = new ArrayList<>();
        for(JsonValue project : json.get("projects").values()) {
            person.projects.add(Project.fromJson(project)));
        }
        return person;
    }
}

public abstract Project implements JsonSerializable {
    
    public static Project fromJson(JsonValue object) {
        switch(object.getString("type")) {
        case "external":
            return ExternalProject.fromJson(object);
        case "internal":
            return InternalProject.fromJson(object);
        }
    }
}
```

In this case, you have to provide your own serialization/deserialization 
logic, so there is now a cost for moving between Java objects and
JavaScript objects/JSON strings.

However, the `JsonValue` interface is implemented by a single [JavaScript Overlay Object](http://www.gwtproject.org/doc/latest/DevGuideCodingBasicsOverlay.html) 
in compiled code, so at least there is no cost moving between, for example,
a JavaScript object returned by the IndexedDB API and a `JsonValue` reference.

For example, Java code like:

```.java
JsonValue object;
model.quantity = object.getNumber("quantity");
```

will compile directly to the JavaScript:

```.js
model.quantity = +object['quantity'];
```

## Design Choices

Given the many good JSON libraries available for the JVM, it may not be immediately
clear why ActivityInfo needs its own JSON library.

Here are a few of the approaches and libraries considered:

TODO
