var LEVEL_DEBUG = 0;var LEVEL_INFO = 1;var LEVEL_WARN = 2;var LEVEL_ERROR = 3;function Logger(logLevel) {    this.level = logLevel;    this.logConsole = document.getElementById('logging-console');    this.logList = document.getElementById('log-list');    this.hide();}Logger.prototype.show = function() {   this.logConsole.style.display = "";};Logger.prototype.hide = function() {   this.logConsole.style.display = "none";};Logger.prototype.clear = function() {    while (this.logList.hasChildNodes()) {        this.logList.removeChild(this.logList.firstChild);    }};Logger.prototype.debug = function(message) {    if (this.level <= LEVEL_DEBUG) {        this.log(message, "debug");    }};Logger.prototype.info = function(message) {    if (this.level <= LEVEL_INFO) {        this.log(message, "info");    }};Logger.prototype.warn = function(message) {    if (this.level <= LEVEL_WARN) {        this.log(message, "warn");    }};Logger.prototype.error = function(message) {    if (this.level <= LEVEL_ERROR) {        this.log(message, "error");    }};Logger.prototype.log = function(message, className) {    var loggingNode = document.createElement('li');    loggingNode.className = className;    loggingNode.appendChild(document.createTextNode(message));    this.logList.appendChild(loggingNode);    this.show();};