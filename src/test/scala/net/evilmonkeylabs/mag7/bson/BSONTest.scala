package net.evilmonkeylabs.mag7.bson

import org.specs2._
import org.junit.runner._
import runner._

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
      	"Parsing returns a valid document" ! testBasicParse ^ 
      														 end

  lazy val javaBSON = {
    val oid = new org.bson.types.ObjectId
    val testOid = new org.bson.types.ObjectId
    val testRefId = new org.bson.types.ObjectId

    val testDoc = new com.mongodb.BasicDBObject
    testDoc.put("foo", "bar");
    testDoc.put("x", 5.23);

    val testList = new java.util.ArrayList[String]
    testList.add("foo");
    testList.add("bar");
    testList.add("baz");
    testList.add("x");
    testList.add("y");
    testList.add("z");

    val testTsp = new org.bson.types.BSONTimestamp();

    val testDate = new java.util.Date();

    /* BINARY
	val testBin = new Binary("foobarbaz".getBytes())
	val testUUID = UUID.randomUUID()
	*/
    val testRE = Pattern.compile("^test.*regex.*xyz$", Pattern.CASE_INSENSITIVE);

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
    // Symbol wonky
    b.append("symbol", new org.bson.types.Symbol("foobar"));
    // Code wonky
    b.append("code", new org.bson.types.Code("var x = 12345;"));
    // TODO - Shell doesn't work with Code W/ Scope, return to this test later
    /*
    b.append( "code_scoped", new CodeWScope( "return x * 500;", test_doc ) );*/
    b.append("str", "foobarbaz");
    //b.append("ref", new com.mongodb.DBRef(_db, "testRef", test_ref_id));
    b.append("object", testDoc);
    b.append("array", testList);
    //b.append("binary", testBin);
    //b.append("uuid", testUUID);
    b.append("regex", testRE);

    val doc = b.get()
    
    val encoder = new org.bson.BasicBSONEncoder
    
    java.nio.ByteBuffer.wrap(encoder.encode(doc))
  }
  
  lazy val parsedBSON = {
    val p = new DefaultBSONDocParser(javaBSON) 
    p.result()
  }
  
  def testBasicParse = {
    parsedBSON must not beNull
  }
}