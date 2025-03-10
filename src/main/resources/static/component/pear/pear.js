window.rootPath = (function(src) {
	src = document.scripts[document.scripts.length - 1].src;
	return src.substring(0, src.lastIndexOf("/") + 1);
})();

layui.config({
	base: rootPath + "module/",
	version: true
}).extend({
	admin: "admin",
	menu: "menu",
	frame: "frame",
	tab: "tab",
	echarts: "echarts",
	echartsTheme: "echartsTheme",
	hash: "hash",
	document: "document",
	select: "select",
	drawer: "drawer",
	notice: "notice",
	step:"step",
	tag:"tag",
	popup:"popup",
	iconPicker:"iconPicker",
	treetable:"treetable",
	dtree:"dtree",
	tinymce:"tinymce/tinymce",
	area:"area",
	count:"count",
	topBar: "topBar",
	button: "button",
	design: "design",
	common: "common",
	eleTree: "eleTree",
	xmSelect: 'xm-select'
}).use(['layer'], function () {
	var $ = layui.jquery;
	$.ajaxSetup({
		complete : function(XMLHttpRequest, textStatus) {
			if (XMLHttpRequest.status == 401 ) {
				// 如果超时就处理
				window.location.replace("/login");
			}
		}
	});
});
