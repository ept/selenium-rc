/*
* Copyright 2004 ThoughtWorks, Inc
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*
*/

passColor = "#cfffcf";
failColor = "#ffcfcf";
workingColor = "#DEE7EC";

// The current row in the list of commands (test script)
currentCommandRow = 0;
inputTableRows = null;

// The current row in the list of tests (test suite)
currentTestRow = 0;

// Whether or not the jsFT should run all tests in the suite
runAllTests = false;

// Whether or not the current test has any errors;
testFailed = false;
suiteFailed = false;

// Holds variables that are stored in a script
storedVars = new Object();

// Holds the handlers for each command.
commandHandlers = null;

// The number of tests run
numTestPasses = 0;

// The number of tests that have failed
numTestFailures = 0;

// The number of commands which have passed
numCommandPasses = 0;

// The number of commands which have failed
numCommandFailures = 0;

// The number of commands which have caused errors (element not found)
numCommandErrors = 0;

// The time that the test was started.
startTime = null;

// The current time.
currentTime = null;

// An simple enum for failureType
ERROR = 0;
FAILURE = 1;

runInterval = 0;

function setRunInterval() {
    runInterval = this.value;
}

function continueCurrentTest() {
    testLoop.finishCommandExecution();
}

function getApplicationFrame() {
    return document.getElementById('myiframe');
}

function getSuiteFrame() {
    return document.getElementById('testSuiteFrame');
}

function getTestFrame(){
    return document.getElementById('testFrame');
}

function loadAndRunIfAuto() {
    loadSuiteFrame();
}

function getExecutionContext() {
    if (isNewWindow()) {
        return getWindowExecutionContext();
    }
    else if (isSafari || isKonqueror) {
        return new KonquerorIFrameExecutionContext();
    }
    else {
        return new IFrameExecutionContext();
    }
}

function start() {
    loadSuiteFrame(getExecutionContext());
}

function loadSuiteFrame(executionContext) {

    var testAppFrame = executionContext.loadFrame();
    browserbot = createBrowserBot(testAppFrame,executionContext);
    selenium = new Selenium(browserbot);
    registerCommandHandlers();

    //set the runInterval if there is a queryParameter for it
    var tempRunInterval = getQueryParameter("runInterval");
    if (tempRunInterval) {
        runInterval = tempRunInterval;
    }

    document.getElementById("modeRun").onclick = setRunInterval;
    document.getElementById('modeWalk').onclick = setRunInterval;
    document.getElementById('modeStep').onclick = setRunInterval;
    document.getElementById('continueTest').onclick = continueCurrentTest;

    var testSuiteName = getQueryParameter("test");

    if (testSuiteName) {
        addLoadListener(getSuiteFrame(), onloadTestSuite);
        getSuiteFrame().src = testSuiteName;
    } else {
        onloadTestSuite();
    }
}

function startSingleTest() {
    removeLoadListener(getApplicationFrame(), startSingleTest);
    var singleTestName = getQueryParameter("singletest");
    addLoadListener(getTestFrame(), startTest);
    getTestFrame().src = singleTestName;
}

function getIframeDocument(iframe)
{
    if (iframe.contentDocument) {
        return iframe.contentDocument;
    }
    else {
        return iframe.contentWindow.document;
    }
}

function onloadTestSuite() {
    removeLoadListener(getSuiteFrame(), onloadTestSuite);
    suiteTable = getIframeDocument(getSuiteFrame()).getElementsByTagName("table")[0];

    // Add an onclick function to each link in the suite table
    for(rowNum = 1;rowNum < suiteTable.rows.length; rowNum++) {
        addOnclick(suiteTable, rowNum);
    }


    if (isAutomatedRun()) {
        startTestSuite();
    } else if (getQueryParameter("autoURL")) {

        addLoadListener(getApplicationFrame(), startSingleTest);

        getApplicationFrame().src = getQueryParameter("autoURL");

    } else {
        testLink = suiteTable.rows[currentTestRow+1].cells[0].getElementsByTagName("a")[0];
        getTestFrame().src = testLink.href;
    }
}

// Adds an onclick function to the link in the given row in suite table.
// This function checks whether the test has already been run and the data is
// stored. If the data is stored, it sets the test frame to be the stored data.
// Otherwise, it loads the fresh page.
function addOnclick(suiteTable, rowNum) {
    aLink = suiteTable.rows[rowNum].cells[0].getElementsByTagName("a")[0];
    aLink.onclick = function(eventObj) {
        srcObj = null;

        // For mozilla-like browsers
        if(eventObj)
                srcObj = eventObj.target;

        // For IE-like browsers
        else if (getSuiteFrame().contentWindow.event)
                srcObj = getSuiteFrame().contentWindow.event.srcElement;

        // The target row (the event source is not consistently reported by browsers)
        row = srcObj.parentNode.parentNode.rowIndex || srcObj.parentNode.parentNode.parentNode.rowIndex;

        // If the row has a stored results table, use that
        if(suiteTable.rows[row].cells[1]) {
            var bodyElement = getIframeDocument(getTestFrame()).body;

            // Create a div element to hold the results table.
            var tableNode = getIframeDocument(getTestFrame()).createElement("div");
            var resultsCell = suiteTable.rows[row].cells[1];
            tableNode.innerHTML = resultsCell.innerHTML;

            // Append this text node, and remove all the preceding nodes.
            bodyElement.appendChild(tableNode);
            while (bodyElement.firstChild != bodyElement.lastChild) {
                bodyElement.removeChild(bodyElement.firstChild);
            }
        }
        // Otherwise, just open up the fresh page.
        else {
            getTestFrame().src = suiteTable.rows[row].cells[0].getElementsByTagName("a")[0].href;
        }

        return false;
    };
}

function isQueryParameterTrue(name) {
    parameterValue = getQueryParameter(name);
    return (parameterValue != null && parameterValue.toLowerCase() == "true");
}

function getQueryParameter(searchKey) {
    var clauses = location.search.substr(1).split('&');
    for (var i = 0; i < clauses.length; i++) {
        var keyValuePair = clauses[i].split('=',2);
        var key = unescape(keyValuePair[0]);
        if (key == searchKey) {
            return unescape(keyValuePair[1]);
        }
    }
    return null;
}

function isNewWindow() {
    return isQueryParameterTrue("newWindow");
}

function isAutomatedRun() {
    return isQueryParameterTrue("auto");
}

function resetMetrics() {
    numTestPasses = 0;
    numTestFailures = 0;
    numCommandPasses = 0;
    numCommandFailures = 0;
    numCommandErrors = 0;
    startTime = new Date().getTime();
}

function runSingleTest() {
    runAllTests = false;
    resetMetrics();
    startTest();
}

function startTest() {
    removeLoadListener(getTestFrame(), startTest);

    // Scroll to the top of the test frame
    if (getTestFrame().contentWindow) {
        getTestFrame().contentWindow.scrollTo(0,0);
    }
    else {
        frames['testFrame'].scrollTo(0,0);
    }

    inputTable = getIframeDocument(getTestFrame()).getElementsByTagName("table")[0];
    inputTableRows = inputTable.rows;
    currentCommandRow = 0;
    testFailed = false;
    storedVars = new Object();

    clearRowColours();

    testLoop = initialiseTestLoop();
    testLoop.start();
}

function clearRowColours() {
    for (var i = 0; i <= inputTableRows.length - 1; i++) {
        inputTableRows[i].bgColor = "white";
    }
}

function startTestSuite() {
    resetMetrics();
    currentTestRow = 0;
    runAllTests = true;
    suiteFailed = false;

    runNextTest();
}

function runNextTest() {
    if (!runAllTests)
            return;

    suiteTable = getIframeDocument(getSuiteFrame()).getElementsByTagName("table")[0];

    // Do not change the row color of the first row
    if(currentTestRow > 0) {
        // Make the previous row green or red depending if the test passed or failed
        if(testFailed)
                setCellColor(suiteTable.rows, currentTestRow, 0, failColor);
        else
                setCellColor(suiteTable.rows, currentTestRow, 0, passColor);

        // Set the results from the previous test run
        setResultsData(suiteTable, currentTestRow);
    }

    currentTestRow++;

    // If we are done with all of the tests, set the title bar as pass or fail
    if(currentTestRow >= suiteTable.rows.length) {
        if(suiteFailed)
                setCellColor(suiteTable.rows, 0, 0, failColor);
        else
                setCellColor(suiteTable.rows, 0, 0, passColor);

        // If this is an automated run (i.e., build script), then submit
        // the test results by posting to a form
        if (isAutomatedRun())
                postTestResults(suiteFailed, suiteTable);
    }

    else {
        // Make the current row blue
        setCellColor(suiteTable.rows, currentTestRow, 0, workingColor);

        testLink = suiteTable.rows[currentTestRow].cells[0].getElementsByTagName("a")[0];
        testLink.focus();

        addLoadListener(getTestFrame(), startTest);
        getExecutionContext().open(testLink.href, getTestFrame());
    }
}

function setCellColor(tableRows, row, col, colorStr) {
    tableRows[row].cells[col].bgColor = colorStr;
}

// Sets the results from a test into a hidden column on the suite table.  So,
// for each tests, the second column is set to the HTML from the test table.
function setResultsData(suiteTable, row) {
    // Create a text node of the test table
    var resultTable = getIframeDocument(getTestFrame()).body.innerHTML;
    if (!resultTable) return;

    var tableNode = suiteTable.ownerDocument.createElement("div");
    tableNode.innerHTML = resultTable;

    var new_column = suiteTable.ownerDocument.createElement("td");
    new_column.appendChild(tableNode);

    // Set the column to be invisible
    new_column.style.cssText = "display: none;";

    // Add the invisible column
    suiteTable.rows[row].appendChild(new_column);
}

// Post the results to a servlet, CGI-script, etc.  The URL of the
// results-handler defaults to "/postResults", but an alternative location
// can be specified by providing a "resultsUrl" query parameter.
//
// Parameters passed to the results-handler are:
//      result:         passed/failed depending on whether the suite passed or failed
//      totalTime:      the total running time in seconds for the suite.
//
//      numTestPasses:  the total number of tests which passed.
//      numTestFailures: the total number of tests which failed.
//
//      numCommandPasses: the total number of commands which passed.
//      numCommandFailures: the total number of commands which failed.
//      numCommandErrors: the total number of commands which errored.
//
//      suite:      the suite table, including the hidden column of test results
//      testTable.1 to testTable.N: the individual test tables
//
function postTestResults(suiteFailed, suiteTable) {

    form = document.createElement("form");
    document.body.appendChild(form);

    form.id = "resultsForm";
    form.method="post";
    form.target="myiframe";

    var resultsUrl = getQueryParameter("resultsUrl");
    if (!resultsUrl) {
        resultsUrl = "./postResults";
    }

    var actionAndParameters = resultsUrl.split('?',2);
    form.action = actionAndParameters[0];
    var resultsUrlQueryString = actionAndParameters[1];

    form.createHiddenField = function(name, value) {
        input = document.createElement("input");
        input.type = "hidden";
        input.name = name;
        input.value = value;
        this.appendChild(input);
    };

    if (resultsUrlQueryString) {
        var clauses = resultsUrlQueryString.split('&');
        for (var i = 0; i < clauses.length; i++) {
            var keyValuePair = clauses[i].split('=',2);
            var key = unescape(keyValuePair[0]);
            var value = unescape(keyValuePair[1]);
            form.createHiddenField(key, value);
        }
    }

    form.createHiddenField("result", suiteFailed == true ? "failed" : "passed");

    form.createHiddenField("totalTime", Math.floor((currentTime - startTime) / 1000));
    form.createHiddenField("numTestPasses", numTestPasses);
    form.createHiddenField("numTestFailures", numTestFailures);
    form.createHiddenField("numCommandPasses", numCommandPasses);
    form.createHiddenField("numCommandFailures", numCommandFailures);
    form.createHiddenField("numCommandErrors", numCommandErrors);

    // Create an input for each test table.  The inputs are named
    // testTable.1, testTable.2, etc.
    for (rowNum = 1; rowNum < suiteTable.rows.length;rowNum++) {
        // If there is a second column, then add a new input
        if (suiteTable.rows[rowNum].cells.length > 1) {
            var resultCell = suiteTable.rows[rowNum].cells[1];
            form.createHiddenField("testTable." + rowNum, getText(resultCell));
            // remove the resultCell, so it's not included in the suite HTML
            resultCell.parentNode.removeChild(resultCell); 
        }
    }

    // Add HTML for the suite itself
    form.createHiddenField("suite", suiteTable.parentNode.innerHTML);

    form.submit();
    document.body.removeChild(form);

}

function printMetrics() {
    setText(document.getElementById("commandPasses"), numCommandPasses);
    setText(document.getElementById("commandFailures"), numCommandFailures);
    setText(document.getElementById("commandErrors"), numCommandErrors);
    setText(document.getElementById("testRuns"), numTestPasses + numTestFailures);
    setText(document.getElementById("testFailures"), numTestFailures);

    currentTime = new Date().getTime();

    timeDiff = currentTime - startTime;
    totalSecs = Math.floor(timeDiff / 1000);

    minutes = Math.floor(totalSecs / 60);
    seconds = totalSecs % 60;

    setText(document.getElementById("elapsedTime"), pad(minutes)+":"+pad(seconds));
}

// Puts a leading 0 on num if it is less than 10
function pad (num) {
    return (num > 9) ? num : "0" + num;
}

/*
 * Search through str and replace all variable references ${varName} with their
 * value in storedVars.
 */
function replaceVariables(str) {
    // We can't use a String.replace(regexp, replacementFunction) since this doesn't
    // work in safari. So replace each match 1 at a time.
    var stringResult = str;
    var match;
    while (match = stringResult.match(/\$\{(\w+)\}/)) {
        var variable = match[0];
        var name = match[1];
        var replacement = storedVars[name];
        stringResult = stringResult.replace(variable, replacement);
    }
    return stringResult;
}

/*
 * Register all of the built-in command handlers with the CommandHandlerFactory.
 * TODO work out an easy way for people to register handlers without modifying the Selenium sources.
 */
function registerCommandHandlers() {
    commandFactory = new CommandHandlerFactory();
    commandFactory.registerAll(selenium);

    // These actions are overridden for fitrunner, as they still involve some FitRunner smarts,
    // because of the wait/nowait behaviour modification. We need a generic solution to this.
    commandFactory.registerAction("click", selenium.doClickWithOptionalWait);

}

function initialiseTestLoop() {
    testLoop = new TestLoop(commandFactory, getExecutionContext());

    testLoop.getCommandInterval = function() { return runInterval; };
    testLoop.firstCommand = nextCommand;
    testLoop.nextCommand = nextCommand;
    testLoop.commandStarted = commandStarted;
    testLoop.commandComplete = commandComplete;
    testLoop.commandError = commandError;
    testLoop.testComplete = testComplete;
    return testLoop;
}

function nextCommand() {
    if (currentCommandRow >= inputTableRows.length - 1) {
        return null;
    }

    currentCommandRow++;

    var commandName = getCellText(currentCommandRow, 0);
    var target = replaceVariables(getCellText(currentCommandRow, 1));
    var value = replaceVariables(getCellText(currentCommandRow, 2));


    var command = new SeleniumCommand(commandName, target, removeNbsp(value));
    return command;
}

function removeNbsp(value)
{
    return value.replace(/\240/g, "");
}

function focusOnElement(element) {
    if (element.focus) {
        element.focus();
        return;
    }
    var anchor = element.ownerDocument.createElement("a");
    anchor.innerHTML = "!CURSOR!";
    element.appendChild(anchor, element);
    anchor.focus();
    element.removeChild(anchor);
}

function commandStarted() {
    inputTableRows[currentCommandRow].bgColor = workingColor;
    focusOnElement(inputTableRows[currentCommandRow].cells[0]);
    printMetrics();
}

function commandComplete(result) {
    if (result.failed) {
        setRowFailed(result.failureMessage, FAILURE);
    } else if (result.passed) {
        setRowPassed();
    } else {
        setRowWhite();
    }
}

function commandError(errorMessage) {
    setRowFailed(errorMessage, ERROR);
}

function setRowWhite() {
    inputTableRows[currentCommandRow].bgColor = "white";
}

function setRowPassed() {
    numCommandPasses += 1;

    // Set cell background to green
    inputTableRows[currentCommandRow].bgColor = passColor;
}

function setRowFailed(errorMsg, failureType) {
    if (failureType == ERROR)
            numCommandErrors += 1;
    else if (failureType == FAILURE)
            numCommandFailures += 1;

    // Set cell background to red
    inputTableRows[currentCommandRow].bgColor = failColor;

    // Set error message
    inputTableRows[currentCommandRow].cells[2].innerHTML = errorMsg;
    inputTableRows[currentCommandRow].title = errorMsg;
    testFailed = true;
    suiteFailed = true;
}

function testComplete() {
    if(testFailed) {
        inputTableRows[0].bgColor = failColor;
        numTestFailures += 1;
    }
    else {
        inputTableRows[0].bgColor = passColor;
        numTestPasses += 1;
    }

    printMetrics();

    window.setTimeout("runNextTest()", 1);
}

function getCellText(rowNumber, columnNumber) {
    return getText(inputTableRows[rowNumber].cells[columnNumber]);
}

Selenium.prototype.doPause = function(waitTime) {
    selenium.callOnNextPageLoad(null);
    testLoop.pauseInterval = waitTime;
};

// Store the value of a form input in a variable
Selenium.prototype.doStoreValue = function(target, varName) {
    if (!varName) { 
        // Backward compatibility mode: read the ENTIRE text of the page 
        // and stores it in a variable with the name of the target
        value = this.page().bodyText();
        storedVars[target] = value;
        return;
    }
    var element = this.page().findElement(target);
    storedVars[varName] = getInputValue(element);
};

// Store the text of an element in a variable
Selenium.prototype.doStoreText = function(target, varName) {
    var element = this.page().findElement(target);
    storedVars[varName] = getText(element);
};

Selenium.prototype.doSetVariable = function(varName, variableExpression) {
    var value = eval(variableExpression);
    storedVars[varName] = value;
};

Selenium.prototype.doClickWithOptionalWait = function(target, wait) {

    this.doClick(target);

    if(wait != "nowait") {
        return SELENIUM_PROCESS_WAIT;
    }

};
