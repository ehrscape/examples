/*
Copyright (c) 2014 Marand (marand.com)
Author: Marko Pipan
Licensed under the GPLv3 license:
http://www.gnu.org/licenses/gpl.html
*/

AqlFormatter = function () {
    this.formatAql = function (inputAql) {
        var depth = 0;

        var target = [];
        var isLastSpace = false;
        var clause;

        function setLastSpace(text, force) {
            if (typeof text === "number") {
                text = "\n" + "                                                                                 ".slice(-(text + 1) * 4);
            }
            if (isLastSpace) {
                target[target.length - 1] = text;
            } else if (force) {
                isLastSpace = true;
                target.push(text);
            }
        }

        function append(text, type) {
            isLastSpace = (type === undefined || type === null);
            target.push(text);
        }

        CodeMirror.runMode(inputAql, "text/aql", function (text, type) {
            if (type === "keyword") {
                text = text.toUpperCase();
                if (["SELECT", "FROM", "ORDER", "WHERE", "FETCH", "LIMIT"].indexOf(text) >= 0) {
                    clause = text;
                    depth = 0;
                    setLastSpace("\n", true);
                    append(text, type);
                } else if (text === "CONTAINS") {
                    setLastSpace("\n", true);
                    append(text, type);
                } else if (["OR", "AND", "XOR"].indexOf(text) >= 0) {
                    append(text, type);
                    setLastSpace(depth, true);
                } else {
                    //setLastSpace(" ", false);
                    append(text, type);
                }
            } else if (type === "symbol") {
                if (text === "(") {
                    depth++;
                    setLastSpace(" ", false);
                    append(text, type);
                    setLastSpace(depth, true);
                    //setLastSpace(" ", false);
                } else if (text === ")") {
                    setLastSpace("", false);
                    depth++;
                    append(text, type);
                    setLastSpace(" ", true);
                } else if (text === "," && clause === "SELECT") {
                    append(text, type);
                    setLastSpace(depth, true);
                } else {
                    append(text, type);
                }
            } else if (type === undefined || type === null) {
                if (!isLastSpace) {
                    append(" ", type);
                }

            } else {
                append(text, type);
            }
        });

        return target.join("").trim();
    };

    this.highlightAql = function (inputAql, targetElement) {
        CodeMirror.runMode(inputAql, "text/aql", targetElement);
    };

    this.highlightJson = function (inputJson, targetElement) {
        CodeMirror.runMode(inputJson, "text/javascript", targetElement);
    };
};
