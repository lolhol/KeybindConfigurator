# How to use the KeybindConfigurator

## What is this?

This is a configuration loading library designed to load keybindings or other configuration settings from a `.toml` file. As can be imagined, the `.toml `is local to each person. Thus each person can have a list of functions that they can run.

## Testing Class

A key concept in this library is the `Testing` class instance. The `Testing` class serves as a collection of functions that are triggered as needed. The specific function names that will be called are defined in the corresponding `.toml` file.

## Basic Usage

Here's an example of how a "Testing" class might look:

```java
public class Testing {
   public void CustomXTest() {
       System.out.println("TEST!"); // will print "TEST!"
   }
}
```

A corresponding `.toml` file would be structured like this:

```toml
[CustomXTest]
```

In the configuration file, the name `"CustomXTest"` matches the name of the function in the `Testing` class. This means that the code will look for a function with that name and call it with no parameters.

## More Advanced: Custom Parameters

You're not limited to just one parameter in this library. By adding other parameters to the .toml file, you can tell the program to input the function with the name of the main variable (CustomXTest in this case) with params that follow the name (“name”).

```toml
[CustomXTest]
arg0 = "TestName"
```

And the corresponding `Testing` file would be:

```java
public class Testing {
   public void CustomXTest(String something) {
       System.out.println(something); // will print "TestName"
   }
}
```

Note that the variables do not have to have the same name **AS LONG AS THEIR TYPES ARE IN THE SAME ORDER AS THE FUNCTION’S TYPES.**

## To conclude

To conclude, this is all of the code that I got, using this library ->

```
public static void main(String[] args) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
   KeybindConfigurator.runFunctions(
       new File("config.toml"),
       new Testing());
}

public class Testing {
   public void CustomXTest(String something) {
       System.out.println(something);
   }
}

[CustomXTest]
name = "TestName"


---
```

## How to implement

Go to [https://jitpack.io/#lolhol/KeybindConfigurator/0.1](https://jitpack.io/#lolhol/KeybindConfigurator/0.1) for detailed explanation of how to implement.

OR

Basically add this to “repositories”:

```
repositories {
   mavenCentral()
   maven { url 'https://jitpack.io' }
}
```

After doing that, add

```
implementation 'com.github.lolhol:KeybindConfigurator:0.2'
```

To your “dependencies”.

## Why use it?

This library is extremely useful not only for production code but also for testing code.

### Testing

1. All the people will have their local functions and thus their local configs (with the function names set to their own values). This will lead to easier debugging.
2. If you want to test a different global variable, you can do it with ease without being scared that the production code will be affected.

### Production Benefits

1. Will allow a better config system rather than just one Global.java file with a bunch of static variables
2. One entry point
   1. Every keybind will be bound to one entry point meaning that there will only be one individual function at which they are defined.
   2. No need for global variables that can be put everywhere. Instead, you are forced to create initializers and input them with the values you put into the config
3. Testing will not interfere with the main production code since all the testing configs are **local** while the final config will be crafted inside another folder where there will be no point for people to go to to change the variables for testing.
