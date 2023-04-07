
<!--<!DOCTYPE html>-->
<!--<html>-->
<!--<head>-->
<!--    <meta charset="UTF-8">-->
<!--    <title>尚硅谷会员登录页面</title>-->
<!--    &lt;!&ndash;写base标签，永远固定相对路径跳转的结果&ndash;&gt;-->
<!--    <base href="http://localhost:8080/">-->

<!--    <link type="text/css" rel="stylesheet" href="static/css/style.css" >-->
<!--</head>-->
<!--<body>-->
<!--<div id="login_header">-->
<!--    <img class="logo_img" alt="" src="static/img/logo.gif" >-->
<!--</div>-->

<!--<div class="login_banner">-->

<!--    <div id="l_content">-->
<!--        <span class="login_word">Welcome to GoMoKu</span>-->
<!--    </div>-->

<!--    <div id="content">-->
<!--        <div class="login_form">-->
<!--            <div class="login_box">-->

<!--                <div class="msg_cont">-->
<!--                    <b></b>-->
<!--                    <span class="errorMsg">Please input username</span>-->
<!--                </div>-->
<!--                <div class="form">-->
<!--                    <form action="Servlet" method="post">-->
<!--                        <input class="itxt" type="text" placeholder="UserName"-->
<!--                               autocomplete="off" tabindex="1" name="username" />-->
<!--                        <input type="submit" value="submit" id="sub_btn" />-->
<!--                    </form>-->
<!--                </div>-->

<!--            </div>-->
<!--        </div>-->
<!--    </div>-->
<!--</div>-->
<!--</body>-->
<!--</html>-->

<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="utf-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>GoMoKu</title>
    <!--写base标签，永远固定相对路径跳转的结果-->
    <base href="http://47.120.38.17:8080/">
<%--    <base href="http://127.0.0.1:8080/">--%>
    <link type="text/css" rel="stylesheet" href="/static/css/style.css" >
</head>
<script src="/static/script/jquery-1.7.2.js"></script>
<script type="text/javascript">
    function validate(obj) {
        console.log(obj.value.length)
        // return false
        if (obj.value) {
            return true
        } else {
            alert("please input the UserName!")
            return false
        }
    }



</script>

<body>
<div id="login_header" style="text-align: center">
<%--    <img class="logo_img" alt="" src="static/img/logo.gif" >--%>
</div>

<div class="login_banner">

    <div id="l_content" style="margin-left: 150px;">
        <span class="login_word">Welcome to GoMoKu!</span>
    </div>

    <div id="content" >
        <div class="login_form">
            <img style="display:block;margin-left:auto;margin-right:auto;margin-top: 15px" alt="" src="/static/img/logo.gif" >
            <div class="login_box" style="margin-top: 15px">

                <div class="msg_cont">
                    <b></b>
                    <span class="errorMsg">Please input username!</span>
                </div>
                <div class="form">
                    <form action="index" method="post" onsubmit="return validate(document.getElementById('myText'));">
                        <br />
                        <label>USERNAME：</label>
                        <input class="itxt" type="text" placeholder="UserName"
                               autocomplete="off" tabindex="1" name="username" id="myText"/>
                        <br />
                        <br />
                        <input type="submit" value="Enter the game" id="sub_btn" />
                        </form>
                    </div>

                </div>
            </div>
    </div>
</div>
</body>
</html>