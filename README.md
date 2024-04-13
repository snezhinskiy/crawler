<h1>Web crawler/scraper.</h1>

**KEYWORDS: multithreading, VirtualThreads, SmartLifecycle, Jsoup, Java 21**

Starting this project, I was inspired by the idea of trying out virtual streams 
introduced in Java 21, as well as building a project based on the utilization of
a tool like <a href="https://github.com/snezhinskiy/crawler/blob/main/src/main/java/com/snezhinskiy/crawler/processing/service/impl/CrawlerLifecycleService.java">SmartLifecycle</a>.

Additionally, the project should meet the following functional requirements:
- **Flexibility and extensibility**: The basic functionality should be 
  available out of the box, but the core services of the project 
  should be easily customizable.

- **Various resource traversal strategies**: Currently, three strategies are 
  supported: 
    (1) full recursive traversal, 
    (2) traversal of links using CSS selectors, 
    (3) downloading individual pages.

- **Simultaneous downloading and processing** of data from multiple sources.

- **Delay interval between page loading** (from the same resource) to avoid 
  creating significant load on the resource.

- **Declarative description of parser instructions** for web pages: without 
  the need to write logic using Java.

- Ability to schedule ad-hoc download tasks by calling the API.

- API for downloading results.


<h3>Overall Structure</h3>
Internally, data processing builds like a conveyor belt in which three data generators 
participate: <a href="https://github.com/snezhinskiy/crawler/blob/main/src/main/java/com/snezhinskiy/crawler/processing/service/impl/Crawler.java">Crawler</a>,
<a href="https://github.com/snezhinskiy/crawler/blob/main/src/main/java/com/snezhinskiy/crawler/processing/service/impl/ContentParser.java">ContentParser</a>, 
and <a href="https://github.com/snezhinskiy/crawler/blob/main/src/main/java/com/snezhinskiy/crawler/processing/service/impl/LinksParser.java">LinksParser</a>, which are closed in a loop. These 
three components are interconnected via asynchronous service(queues), serving both as 
data highways and buffers: 
<a href="https://github.com/snezhinskiy/crawler/blob/main/src/main/java/com/snezhinskiy/crawler/processing/service/impl/InMemoryCrawlerJobService.java">InMemoryCrawlerJobService</a>, 
<a href="https://github.com/snezhinskiy/crawler/blob/main/src/main/java/com/snezhinskiy/crawler/processing/service/impl/InMemoryLinksParserJobService.java">InMemoryLinksParserJobService</a>, 
<a href="https://github.com/snezhinskiy/crawler/blob/main/src/main/java/com/snezhinskiy/crawler/processing/service/impl/InMemoryContentParserJobService.java">InMemoryContentParserJobService</a>.
<br>
<img src="https://github.com/snezhinskiy/crawler/blob/main/scheme.jpg" />
<br>

<h3>Flexibility and scalability</h3>
I want to point out separately that, since the project is non-commercial, I limited 
myself to using inMemory queues, although they may be potentially expanded. In other 
words, one can write their own implementation of the interface, which will use a 
key/value storage, Message Broker, or a database for data storage.

Also, I want to point out the fact that I significantly simplified the data loading 
process. In a real project, it is worth using a pool proxy and more complex algorithms
to pass anti-bot checks.

<h3>Different resource traversal strategies</h3>
In general, the system operates with terms of configurations and tasks that are created 
according to their configurations. Configurations are entities represented by:
  ```JobConfig @Table(name="job_config")```
and a job is represented by: 
  ```Job @Table(name="job")```

The traversal strategy for resources is specified in the field JobConfig.uploadMethod 
and can take values from the <a href="https://github.com/snezhinskiy/crawler/blob/main/src/main/java/com/snezhinskiy/crawler/domain/embedded/UploadMethod.java">UploadMethod</a> Enum.


<h3>The interval delay between page loads</h3>
Basic services, including the Crawler, operate in multithreaded mode. The number of 
threads is set in the _application.yml_ file using the _crawler.maxWorkers_ property.

<h3>Declarative description of instructions for web page parsing</h3>
To prevent the necessity for modifying the source code for each resource, I created 
a small framework that allows building a kind of data processing pipeline. To 
familiarize yourself with the basic blocks, take a look at the package: 
<a href="https://github.com/snezhinskiy/crawler/tree/main/src/main/java/com/snezhinskiy/crawler/processing/parser/chain/processor">com.snezhinskiy.crawler.processing.parser.chain.processor</a>

Here you will find several simple data handlers, which can be conditionally divided 
into several categories:

**Generative**
- CssSelector
- TagSelector

They take an input of a Document object from the org.jsoup package and extract primary 
data, which can be obtained in the form of either a list of Elements, List<String>, 
String, or Map<String, Object>. The specific format of the result is determined by 
the handler next in the chain of calls.

**Filtering**
- Filter ```(EMPTY | NOT_EMPTY | CONTAINS | NOT_CONTAINS | EQUALS | NOT_EQUALS | 
STARTS_WITH | NOT_STARTS_WITH | ENDS_WITH | NOT_ENDS_WITH)```
- Limiter - limits the number of items in the list
- Matcher - checks conformity to a regular expression pattern

**Modifiers**
- Flattener - converts a list to a string based on ```FIRST | LAST | JOIN``` principle
- MapFlattener - converts a Map to a string
- Replacer - replaces occurrences according to a regular expression
- Splitter - performs the opposite operation of Flattener, namely converts a 
string to a list using a delimiter
- TagsStripper
- Trimmer - removes characters at the beginning and end of a string
- JsonToMapParser - parses JSON into a Map
- ElementsCombiner - combines data from two streams into a Map. For example, the first
stream parses SKU and the second Price. The output will be Map<SKU, Price>, if the 
data turns out to be valid.

The configuration of chains (or pipes) is stored in the source_parser_map table. 
Each handler is configured using JSON, for example:

```{"type":"FILTER", "arguments":["CONTAINS", ""@type": "Product""]}```

Here:
  Handler type - FILTER (corresponds to Filter)
  arguments - match type and desired substring

So, various handlers can be combined into chains. In this case, the call to 
handlers will propagate from bottom to top. Here's what a complete configuration 
example looks like for obtaining Map<SKU, Price>:

```
"modificationPriceRules":[
   {"type":"TAG_SELECTOR", "arguments":["script", "TEXT"]},
   {"type":"FILTER", "arguments":["CONTAINS", "@context"]},
   {"type":"FILTER", "arguments":["CONTAINS", "\"@type\": \"Product\""]},
   {"type":"ELEMENTS_COMBINER", "arguments":[
       [
           {"type":"MATCHER", "arguments":["(?<=offers\\\":)([^<]+)"]},
           {"type":"SPLITTER", "arguments":["\\{"]},
           {"type":"MATCHER", "arguments":["(?<=sku\\\":\\s*\")([^\\\"]+)"]},
           {"type":"TRIMMER", "arguments":["\""]}
       ],
       [
           {"type":"MATCHER", "arguments":["(?<=offers\\\":)([^<]+)"]},
           {"type":"SPLITTER", "arguments":["\\{"]},
           {"type":"MATCHER", "arguments":["(?<=price\\\":\\s*\")([^\\\"]+)"]},
           {"type":"TRIMMER", "arguments":["\""]}
       ]
   ]}
 ]
```
For more details, you can refer to the <a href="https://github.com/snezhinskiy/crawler/blob/main/src/test/java/com/snezhinskiy/crawler/processing/parser/MultiModificationsProductParserTest.java">MultiModificationsProductParserTest</a> test.
