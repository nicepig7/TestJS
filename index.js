$(function() {
  console.log("hello?");
  //1. add prev/next page
  $('body').panelSnap();
  console.log("y panel snap doesn't work?")
  /*$(".scroll,.scroll-btn").click(function(e) {
    e.preventDefault();
    $.scrollify.next();
  });*/
  // 2. syntax highlight all
  SyntaxHighlighter.all();
});

//NFD0164.dll