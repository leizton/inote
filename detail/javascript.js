var ajax = new XMLHttpRequest();
ajax.open("GET", "/result/listlast.do?from=" + vnext + "&size=3", false);
ajax.onreadystatechange = function () {
    if (ajax.status == 200) {
        var jsonObj = eval("(" + ajax.responseText + ")"); // 获取返回json对象
    }
};
ajax.send();