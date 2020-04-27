<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/common.jsp"%>
<!DOCTYPE html>
<html>
<head>
<title>选座位</title>
<script type="text/javascript">
	$(function() {

		//日期选择
		$("#s_date").combobox({
			url : "${path }/seat/dateCombo.htm", 
			valueField : "value",
			textField : "label",
			panelHeight : "auto",
			editable : false, //不允许手动输入
			onLoadSuccess : function() { //数据加载完毕事件
				var data = $('#s_date').combobox('getData');
				$('#s_date').combobox('select', data[0].value);//显示结果集中的第一条数据
			}
		});

		//时间选择
		$("#s_time").combobox({
			url : "${path }/seat/timeCombo.htm", 
			valueField : "value",
			textField : "label",
			panelHeight : "auto",
			editable : false, //不允许手动输入
			onLoadSuccess : function() { //数据加载完毕事件
				var data = $('#s_time').combobox('getData');
				$('#s_time').combobox('select', data[0].value);
			}
		});

		//阅览室选择
		$("#s_room").combobox({
			url : "${path }/seat/roomCombo.htm", //获取所有私有域
			valueField : "id",
			textField : "name",//在text框中显示的值
			panelHeight : "auto",
			editable : false, //不允许手动输入
			onLoadSuccess : function() { //数据加载完毕事件
				var data = $('#s_room').combobox('getData');
				$('#s_room').combobox('select', data[0].id);
			}
		});
		//展示座位
		searchSeat();
		//标记自己的座位
		remarkMyselfseat();
	});

	//展示所有座位
	function searchSeat() {
		var date = $("#s_date").combobox("getValue");
		var time = $("#s_time").combobox("getValue");
		var roomid = $("#s_room").combobox("getValue");
		$.ajax({
			url : '${path}/seat/combolist.htm',
			data : {
				"roomid" : roomid,
				"date" : date,
				"time" : time
			},
			async : false,
			type : 'post',
			dataType : 'json',
			success : function(result) {
				var l = result.length;//在某一天某一时间段的某个房间里的所有座位数
				
				//取最后一个座位的行列信息，即可得出整个房间的行列信息
				var rowMax = result[l - 1].row;
				var colMax = result[l - 1].col;
				
				var str = "<tr><td></td>";
				for (var i = 0; i < colMax; i++) {
					str += "<td>&nbsp;</td><td>&nbsp;</td><td>"+ (i + 1) + "列</td>";
				}
				str += "</tr>";
				for (var i = 1; i <= rowMax; i++) {
					str += "<tr><td>第" + i + "排</td>";
					for (var j = 1; j <= colMax; j++) {
						var seat = result[(i - 1) * colMax + j - 1]; //第i排第j列在ajax返回的json列表中的位置信息
						var id = seat.date + "-" + seat.time + "-"+ seat.roomid + "-" + i + "-" + j;//==seat.keyword
						str += "<td>&nbsp;</td><td>&nbsp;</td><td>";
						//设置座位的形状及点击事件
						str += "<section title='.squaredOne'>";
						str += "<div class='squaredOne' ";
						//座位没有被选中的可以执行点击事件
						if (seat.studentno == "1") {
							str += " onclick='sel(\"" + id + "\")' ";
						}
						str += " >";
						str += "<input type='checkbox' value='"+ seat.studentno + "' id='" + id + "' name='check'";
						if (seat.studentno != "1" && i == seat.row && j == seat.col) {
							str += " checked disabled";//被选中的座位无法再被点击
						}
						str += "/>";
						//label标签的for属性规定 label 与哪个表单元素绑定。
						str += "<label for='squaredOne' id='"+id+"LABEL'></label></div></section>";
						str += "</td>";
					}
					str += "</tr>";
				}
				$("#tableID").html(str);
			},
			error : function(error) {
				console.log(error);
			}
		});
		remarkMyselfseat();
	}

	// 个人选座样式变化
	function sel(id) {
		var oldValue = $("#oldLabel").val();
		if (id == oldValue) { //说明两次点在一个上面，表示取消
			$("#newLabel").val("");
			$("#oldLabel").val("");
			$(document.getElementById(oldValue + "LABEL")).after(function() {
				$(this).css({
					"background" : "#38393d"
				});
			});
			return;
		}

		// 去掉旧值样式
		var oldLabelID = oldValue + "LABEL";
		$(document.getElementById(oldLabelID)).after(function() {//$(selector).after(function(index)):使用函数在被选元素之后插入指定的内容
			$(this).css({
				"background" : "#38393d"
			});
		});

		// 改变此次样式
		var a = document.getElementById(id);
		
		//nextAll('label')表示a之后的所有label元素
		//eq() 方法返回带有被选元素的指定索引号的元素。
		var labelID = $(a).nextAll('label').eq(0).attr("id");
		var l = document.getElementById(labelID);
		$(l).after(function() {
			$(this).css({
				"background" : "red"
			});
		});
		// 设置新值
		$("#newLabel").val(id);
		$("#oldLabel").val(id);
	}

	//标记自己的座位
	function remarkMyselfseat() {
		var date = $("#s_date").combobox("getValue");
		var time = $("#s_time").combobox("getValue");
		var roomid = $("#s_room").combobox("getValue");

		$.ajax({
			url : 'myselfSeat.htm',
			data : {
				"roomid" : roomid,
				"date" : date,
				"time" : time
			},
			async : false,
			type : 'post',
			success : function(result) {//result为choice.getSeatkeyword(),格式比如为:2020-04-25-08:00-12:00-1-1-3
				//在当前日期，时间段和房间里没有座位被选择
				if (result == "no") {
					//不做操作
				} else {
					//标记颜色。并设置值
					$("#newLabel").val(result);
					$("#oldLabel").val(result);

					$("#mySeat").val(result);
					$(document.getElementById(result + "LABEL")).after(//相当于设置label标签的样式
							function() {
								$(this).css({
									"background":"red"
								});
							});
				}
			}
		});
	}

	// 保存选座
	function saveSeat() {
		var keyword = $("#newLabel").val();
		if (keyword == null || keyword.length == 0) {
			$.messager.alert('系统提示', "请选择一个位置");
			return;
		}
		$.ajax({
			url : 'saveSeat.htm',
			data : {
				keyword : keyword
			},
			success : function(result) {
				if (result == "ok") {
					$.messager.alert('系统提示', "成功预约");
					searchSeat();
				} else {
					$.messager.alert('系统提示', result);
				}
			}
		});
	}

	// 取消选座
	function cancelSeat() {
		var oldKey = $("#mySeat").val();
		if (oldKey == null || oldKey.length == 0) {
			$.messager.alert('系统提示', "您未在该阅览室的这个日期时间段选座！");
			return;
		} else {
			$.ajax({
				url : 'cancelSeat.htm',
				data : {
					"seatkeyword" : oldKey
				},
				type : 'post',
				success : function(data) {
					if (data == "ok") {
						$.messager.alert('系统提示', "取消成功");
						searchSeat();
					} else {
						$.messager.alert('系统提示', result);
						return;
					}
				}
			});
		}
	}
</script>
</head>
<body style="margin: 1px;">

	<input id="oldLabel" type="hidden" />
	<input id="newLabel" type="hidden" />
	<input id="mySeat" type="hidden" />
	<table title="座位信息【说明：绿色表示已选，黑色表示可选，红色表示您】 " class="easyui-datagrid"
		toolbar="#tb">
	</table>
	<div id="tb">
		<div class="updownInterval"></div>
		<div>
			&nbsp;日期&nbsp;<input class="easyui-combobox" id="s_date" size="20" />&nbsp;&nbsp;
			&nbsp;时间段&nbsp;<input class="easyui-combobox" id="s_time" size="20" />&nbsp;&nbsp;
			&nbsp; 阅览室&nbsp;<input class="easyui-combobox" id="s_room" size="20" /> 
			<a href="javascript:searchSeat()" class="easyui-linkbutton" iconCls="icon-search">查询</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 
			<a href="javascript:saveSeat()" class="easyui-linkbutton" iconCls="icon-ok">保存位置</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 
			<a href="javascript:cancelSeat()" class="easyui-linkbutton" iconCls="icon-cancel">取消占座</a>
		</div>
		<div class="updownInterval"></div>
	</div>
	<table id="tableID" align="center">

	</table>

</body>
</html>