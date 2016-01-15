var x = 0;
function test01() {
  log("local before :", x);
  var x = 10;
  log("local after :", x);
}
log("global before :", x);
test01();
log("global after :", x);

//----------- util function ---------------//
function log(){
  console.log.apply(console, arguments);
};