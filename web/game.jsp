
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="utf-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>GoMoKu</title>
    <!--写base标签，永远固定相对路径跳转的结果-->
    <base href="http://47.120.38.17:8080/">

    <link type="text/css" rel="stylesheet" href="/static/css/style.css" >
</head>
<script src="/static/script/jquery-1.7.2.js"></script>
<style type="text/css">
    .td0 {
        width: 50px;
        height: 50px;
        border-style: solid;
    }
    .td1 {
        background: url('/static/img/blackStone.gif') no-repeat center;
    }
    .td2 {
        background: url('/static/img/whiteStone.gif') no-repeat center;
    }
    .toast {
        display: none;
        position: fixed;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        width: 18rem;
        height: 18rem;
        left: 50%;
        top: 50%;
        transform: translate(-50%, -50%);
        background-color: rgba(0,0,0,0.2);
        border-radius: 1rem;
        color: #f0f0f0;
        font-size: 2.5rem;
    }

    .load {
        display: inline-block;
        margin-bottom: 1.5rem;
        height: 4rem;
        width: 4rem;
        border: 0.4rem solid transparent;
        border-top-color: white;
        border-left-color: white;
        border-bottom-color: white;
        animation: circle 1s infinite linear;
        -webkit-animation: circle 1s infinite linear; /* Safari 和 Chrome */
        border-radius: 50%
    }

    @-webkit-keyframes circle {
        0% {
            transform: rotate(0deg);
        }
        100% {
            transform: rotate(-360deg)
        }
    }
</style>

<body>
    <div class="toast" id="toast">
        <span class="load"></span>
        <span>matching...</span>
    </div>
    <div style="flex-direction: column; text-align: center; width: 500px; margin-top: 50px">
        
        <div style="background-color: #e3e3e3; display: flex; flex-direction:row; justify-content:space-between">
            <div style="display:inline; border:solid 2px">Player1:  ${username}</div>
            <div style="display:inline; border:solid 2px" id="opponent"></div>
        </div>
        <div id="result" style="background-color: #e3e3e3">Game in progress!</div>
        <div style="background: url('/static/img/background.gif'); ">
            <table border="1">
                

            </table>
        </div>
    </div>
</body>
<script>
    // 1. 利用table构建10*10的棋盘
    let tb = document.getElementsByTagName("table")[0]
    html = ""
    for(let i=0; i<10; i++){
        html += "<tr>"
        for (let j=0; j<10; j++){
            html = html+'<td class="td0" '+'id="td_'+i+'_'+j+'"></td>'
        }
        html += "</tr>"
    }
    // console.log(html)
    tb.innerHTML += html

    // 2. 获取用户名
    const username = '${username}'
    // 3. 开启webstocket服务的ip地址  ws:// + ip地址 + 访问路径 https://36428sh062.imdo.co/
    var ws = new WebSocket('ws://47.120.38.17:8080/websocket/'+username);

    // 4. 变量初始化
    var game_id = -1;
    color = ""      // 1黑色，2白色
    var flag = -1   // 1轮到自己下，0轮到对方下 (黑色先下)
    var opponent = ""   // 对手
    td1 = 'url("/static/img/blackStone.gif") no-repeat center'
    td2 = 'url("/static/img/whiteStone.gif") no-repeat center'
    limitConnect = 0
    // 棋盘二维数组
    var arr= new Array(10).fill(0)
    for(let i=0;i<arr.length;i++){
        arr[i]=new Array(10).fill(0)
    }

    // 5. 添加监听事件
    let list = document.getElementsByTagName('td')
    console.log("td list length:"+list.length)
    for (let i=0; i<list.length; i++){
        list[i].num = i
        list[i].onclick = function(){
            if(flag==0) {
                alert("It is not your turn!")
                return
            }
            let num = this.num
            let x = Math.floor(num / 10)
            let y = num % 10
            console.log(num,x,y)
            console.log(arr[x][y])
            console.log(this.id,game_id)
            if (arr[x][y]==0 && game_id != -1){
                arg = {'x':x, 'y':y, 'color': color, 'game_id':game_id, 'name': username}
                this.style.background = color==1 ? td1 : td2;
                console.log(arg) 
                ws.send(JSON.stringify(arg))
                flag=0
            }
        }
    } 
    // 6. 接收来自服务端的信息
    function init() {
        
        // var ws = new WebSocket('ws://127.0.0.1:8080/websocket/'+username);
        //监听是否连接成功
        ws.onopen = function () {
            ws.send('{"msg":"建立连接"}')
            console.log('ws连接状态：' + ws.readyState);
            // 等待
            var toast = document.getElementById("toast");
            // console.log(toast)
            toast.style.display = "flex";
        }
        // 接听服务器发回的信息并处理展示
        ws.onmessage = function (data) {

            res1 = eval("("+data.data+")")
            console.log("receive:"+res1)
            console.log("msg:"+res1['msg'])
            console.log(typeof(res1['flag'])=="undefined")
            console.log("game_id:"+res1['game_id']+ " x:" + res1['x'] + " y:" + res1['y'] + " flag:"+res1['flag'])

            if(typeof(res1['game_id']) != "undefined") {                                    // 开始下棋
                game_id = res1['game_id']
                color = res1['color'] == "black" ? 1 : 2
                opponent = res1['opponent']
                $('.toast').css({display: 'none'})
                document.getElementById("opponent").innerHTML = "Player2:  "+opponent
                flag = color == 1 ? 1 : 0
            } else if (typeof(res1['x'])!="undefined" && typeof(res1['y'])!="undefined") {    // 同步对方下的棋
                let x = res1['x']
                let y = res1['y']
                arr[parseInt(x)][parseInt(y)] = (color + 1)%2
                flag = 1                            // 轮到自己下
                document.getElementById('td_'+x+'_'+y).style.background = color==1 ? td2 : td1;
            } else if (typeof(res1['flag'])!="undefined") {
                //接收到 消息后给后端发送的 确认收到消息，后端接收到后 不再重复发消息
                ws.send('{"msg":"已接收到消息"}');
                if (res1['flag'] == 'true') {
                    alert('You Win! Congratulations!')
                    document.getElementById("result").innerHTML = ""+username+" win !"
                } else {
                    alert('You Lose!')
                    document.getElementById("result").innerHTML = ""+opponent+" win !"
                }
                //完成通信后关闭WebSocket连接
                ws.close();
            }
            
        }
// 监听连接关闭事件
        ws.onclose = function () {
            // 监听整个过程中websocket的状态
            console.log('ws连接状态：' + ws.readyState);
            reconnect();

        }
// 监听并处理error事件
        ws.onerror = function (error) {
            console.log(error);
        }
        console.log("init finished!")

    }
    function reconnect() {
        limitConnect ++;
        console.log("重连第" + limitConnect + "次");
        setTimeout(function(){
            init();
        },2000);

    }
    init()
    console.log("init end!")
    // 判断并进行游戏等
</script>
</html>