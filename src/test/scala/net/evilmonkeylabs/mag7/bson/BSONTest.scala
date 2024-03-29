package net.evilmonkeylabs.mag7.bson

import org.specs2._
import org.junit.runner._
import runner._

import scala.collection.JavaConverters._

import net.evilmonkeylabs.mag7.bson.doc._
import net.evilmonkeylabs.mag7.bson.types._

import java.util.regex._

@RunWith(classOf[JUnitRunner])
class BSONTest extends Specification {
  def is =

    "This is a specification to test the functionality of BSON" ^
      p ^
      "Parsing of BSON should" ^
      "Provide clear, valid, and sane interop w/ old Java driver" ^
      "Parsing returns a valid document, checking fields" ! testBasicParse ^
      "_id" ! hasOID ^
      "null" ! hasNull ^
      "maxKey" ! hasMax ^
      "minKey" ! hasMin ^
      "booleanTrue" ! hasBoolTrue ^
      "booleanFalse" ! hasBoolFalse ^
      "int1" ! hasInt1 ^
      "int1500" ! hasInt1500 ^
      "int3753" ! hasInt3753 ^
      "tsp" ! hasTsp ^
      "date" ! hasDate ^
      "long5" ! hasLong5 ^
      "long3254525" ! hasLong3254525 ^
      "float324_582" ! hasFloat324_582 ^
      "double245_6289" ! hasDouble245_6289 ^
      "another OID" ! hasOtherOID ^
      "symbol" ! hasSymbol ^
      "code" ! hasCode ^
      "scoped code, code" ! hasScopedCode_Code ^
      "scoped code, scope" ! hasScopedCode_Scope ^
      "str" ! hasStr ^
      "object" ! hasSubObj ^
      "array" ! hasArray ^
      "binary" ! hasBytes ^
      "uuid" ! hasUUID ^
      end

  def testBasicParse = {
    parsedBSON must haveClass[Document] and not beNull
  }

  def hasOID = parsedBSON.get("_id") must haveClass[ObjectID] and beEqualTo(new ObjectID(oid.toString()))

  def hasNull = parsedBSON.get("null") must beNull

  def hasMax = parsedBSON.get("max") must haveClass[BSON.MaxKey]

  def hasMin = parsedBSON.get("min") must haveClass[BSON.MinKey]

  def hasBoolTrue = parsedBSON.get("booleanTrue").asInstanceOf[Boolean] must beTrue

  def hasBoolFalse = parsedBSON.get("booleanFalse").asInstanceOf[Boolean] must beFalse

  def hasInt1 = parsedBSON.get("int1").asInstanceOf[Int] must be_==(1)

  def hasInt1500 = parsedBSON.get("int1500").asInstanceOf[Int] must be_==(1500)

  def hasInt3753 = parsedBSON.get("int3753").asInstanceOf[Int] must be_==(3753)

  def hasTsp = {
    val tsp = parsedBSON.get("tsp").asInstanceOf[BSONTimestamp]
    // TODO - Make this test less stupidly lazy
    tsp.toString must beEqualTo(testTsp.toString)
  }

  def hasDate = parsedBSON.get("date").asInstanceOf[java.util.Date] must be_==(testDate)

  def hasLong5 = parsedBSON.get("long5") must be_==(5L)

  def hasLong3254525 = parsedBSON.get("long3254525") must be_==(3254525L)

  def hasFloat324_582 = parsedBSON.get("float324_582") must be_==(324.582f)

  def hasDouble245_6289 = parsedBSON.get("double245_6289") must be_==(245.6289)

  def hasOtherOID = parsedBSON.get("oid").toString must be_==(testOid.toString)

  def hasSymbol = parsedBSON.get("symbol") must be_==(testSym.getSymbol())

  def hasCode = parsedBSON.get("code").toString must be_==(testCode.getCode().toString)

  def hasScopedCode_Code = parsedBSON.get("code_scoped").asInstanceOf[CodeWScope[Document]].getCode must be_==(testCodeWScope.getCode())
  
  def hasScopedCode_Scope = parsedBSON.get("code_scoped").asInstanceOf[CodeWScope[Document]].getScope().asScala must havePairs("foo" -> "bar", "x"-> 5.23)
  
  def hasStr = parsedBSON.get("str").asInstanceOf[String] must be_==(testStr)
  
  def hasSubObj = parsedBSON.get("object").asInstanceOf[Document].asScala must havePairs("foo" -> "bar", "x" -> 5.23)
      
  def hasArray = parsedBSON.get("array").asInstanceOf[BSONList].asScala must contain("foo", "bar", "baz", "x", "y", "z")
  
  def hasBytes = parsedBSON.get("binary").asInstanceOf[java.nio.ByteBuffer].array() must beEqualTo(testBin.getData())
  
  def hasUUID = parsedBSON.get("uuid").asInstanceOf[java.util.UUID] must beEqualTo(testUUID)
  // -- Setup definitions

  lazy val oid = new org.bson.types.ObjectId

  lazy val testOid = new org.bson.types.ObjectId

  lazy val testRefId = new org.bson.types.ObjectId

  lazy val testDoc = {
    val t = new com.mongodb.BasicDBObject
    t.put("foo", "bar");
    t.put("x", 5.23);
    t
  }

  lazy val testList = {
    val t = new java.util.ArrayList[String]
    t.add("foo");
    t.add("bar");
    t.add("baz");
    t.add("x");
    t.add("y");
    t.add("z");
    t
  }

  lazy val testTsp = new org.bson.types.BSONTimestamp(3600, 42);

  lazy val testDate = new java.util.Date();

  lazy val testRE = Pattern.compile("^test.*regex.*xyz$", Pattern.CASE_INSENSITIVE);

  lazy val testSym = new org.bson.types.Symbol("foobar")

  lazy val testCode = new org.bson.types.Code("var x = 12345;")

  lazy val testBin = new org.bson.types.Binary("foobarbaz".getBytes())

  lazy val testUUID = java.util.UUID.randomUUID()
  
  lazy val testCodeWScope = new org.bson.types.CodeWScope("return x * 500;", testDoc)

  lazy val testStr = "foobarbaz"
   
    
  lazy val javaBSON = {

    val b = com.mongodb.BasicDBObjectBuilder.start()
    b.append("_id", oid)
    b.append("null", null);
    b.append("max", new org.bson.types.MaxKey());
    b.append("min", new org.bson.types.MinKey());
    b.append("booleanTrue", true);
    b.append("booleanFalse", false);
    b.append("int1", 1);
    b.append("int1500", 1500);
    b.append("int3753", 3753);
    b.append("tsp", testTsp);
    b.append("date", testDate);
    b.append("long5", 5L);
    b.append("long3254525", 3254525L);
    b.append("float324_582", 324.582f);
    b.append("double245_6289", 245.6289);
    b.append("oid", testOid);
    // Code wonky
    b.append("code", testCode);
    b.append("code_scoped", testCodeWScope);
    b.append("str", testStr);
    //b.append("ref", new com.mongodb.DBRef(_db, "testRef", test_ref_id));
    b.append("object", testDoc);
    b.append("array", testList);
    b.append("binary", testBin);
    b.append("uuid", testUUID);
    b.append("regex", testRE);
    // Symbol wonky
    b.append("symbol", testSym);

    val doc = b.get()

    val encoder = new org.bson.BasicBSONEncoder

    java.nio.ByteBuffer.wrap(encoder.encode(doc))
  }

  lazy val parsedBSON: Document = {
    val p = new DefaultBSONDocParser(javaBSON)
    p.result()
  }

}