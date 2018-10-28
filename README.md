# Sugar ORM 
Insanely easy way to work with Android databases.

Official documentation can be found [here](http://satyan.github.io/sugar) - Check some examples below. The example application is provided in the **example** folder in the source.


## Features

Sugar ORM was built in contrast to other ORM's to have:

- A simple, concise, and clean integration process with minimal configuration.
- Automatic table and column naming through reflection.
- Support for migrations between different schema versions.
- Automatic modification of tables when changing fields.
- Automatic creation of tables during app installation.

## Installing

In order to install Sugar:

Add it in your root build.gradle at the end of repositories:
```
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

Step 2. Add the dependency
```
	dependencies {
	        implementation 'com.github.mohammadaminha:sugar:v1.0'
	}
```

[Share this version](https://jitpack.io/#mohammadaminha/sugar/v1.0)


### How to use master version
First, download sugar repository
```
git clone git@github.com:mohammadaminha/sugar.git
```

include this in your **settings.gradle**
```gradle
include ':app' // your module app
include ':sugar'

def getLocalProperty(prop) {
	Properties properties = new Properties()
	properties.load(new File(rootDir.absolutePath + '/local.properties').newDataInputStream())
	return properties.getProperty(prop, '')
}

project(':sugar').projectDir = new File(getLocalProperty('sugar.dir'))

```

include this in your **local.properties**
```
sugar.dir=/path/to/sugar/library
```

add sugar project to the dependencies of your main project (build.gradle)
```gradle
dependencies {
    compile project(':sugar')
}
```

You should also comment this line just comment this line (library/build.gradle): https://github.com/satyan/sugar/blob/master/library%2Fbuild.gradle#L2

```gradle
// apply from: '../maven_push.gradle'
```
===================

After installing, check out how to set up your first database in official documentation. Check examples of 1.0 and master below: 

## Examples
### SugarRecord
```java
public class Book extends SugarRecord {
  
  String isbn;
  String title;
  String edition;

  // Default constructor is necessary for SugarRecord
  public Book() {

  }

  public Book(String isbn, String title, String edition) {
    this.isbn = isbn;
    this.title = title;
    this.edition = edition;
  }
}
```
or
```java

public class Book { ... }
```

### Save Entity
```java
Book book = new Book("isbn123", "Title here", "2nd edition")
book.save();
```

or
```java
SugarRecord.save(book); // if using the @Table annotation 
```

### Load Entity
```java
Book book = Book.findById(Book.class, 1);
```

### Update Entity
```java
Book book = Book.findById(Book.class, 1);
book.title = "updated title here"; // modify the values
book.edition = "3rd edition";
book.save(); // updates the previous entry with new values.
```


### Delete Entity
```java
Book book = Book.findById(Book.class, 1);
book.delete();
```

or
```java
SugarRecord.delete(book); // if using the @Table annotation 
```

### Update Entity based on Unique values
```java
Book book = new Book("isbn123", "Title here", "2nd edition")
book.save();

// Update book with isbn123
Book sameBook = new Book("isbn123", "New Title", "5th edition")
sameBook.update();

book.getId() == sameBook.getId(); // true
```

or
```java
SugarRecord.update(sameBook); // if using the @Table annotation 
```

### Bulk Insert
```java
List<Book> books = new ArrayList<>();
books.add(new Book("isbn123", "Title here", "2nd edition"))
books.add(new Book("isbn456", "Title here 2", "3nd edition"))
books.add(new Book("isbn789", "Title here 3", "4nd edition"))
SugarRecord.saveInTx(books);
```

### When using ProGuard
```java
# Ensures entities remain un-obfuscated so table and columns are named correctly
-keep class com.yourpackage.yourapp.domainclasspackage.** { *; }
```

## Contributing

Please fork this repository and contribute back using [pull requests](https://github.com/mohammadaminha/sugar/pulls). Features can be requested using [issues](https://github.com/mohammadaminha/sugar/issues). All code, comments, and critiques are greatly appreciated.

### Thanks to [Original developer](https://github.com/chennaione/sugar)
