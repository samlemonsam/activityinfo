// Based on Spreadsheet mode from
// CodeMirror, copyright (c) by Marijn Haverbeke and others
// Distributed under an MIT license: http://codemirror.net/LICENSE

(function(mod) {
    if (typeof exports == "object" && typeof module == "object") // CommonJS
        mod(require("../../lib/codemirror"));
    else if (typeof define == "function" && define.amd) // AMD
        define(["../../lib/codemirror"], mod);
    else // Plain browser env
        mod(CodeMirror);
})(function(CodeMirror) {
    "use strict";

    CodeMirror.defineMode("activityinfo", function () {
        return {
            startState: function () {
                return {
                    stringType: null,
                    stack: []
                };
            },
            token: function (stream, state) {
                if (!stream) return;

                //check for state changes
                if (state.stack.length === 0) {
                    //strings
                    if ((stream.peek() == '"') || (stream.peek() == "'")) {
                        state.stringType = stream.peek();
                        stream.next(); // Skip quote
                        state.stack.unshift("string");

                    } else if ((stream.peek() == '[') || (stream.peek() == "{")) {
                        var openBracket = stream.next();
                        if(openBracket == '{') {
                            state.closingBracket = '}';
                        } else {
                            state.closingBracket = ']';
                        }
                        state.stack.unshift("symbol");
                    }
                }

                //return state
                //stack has
                switch (state.stack[0]) {
                    case "string":
                        while (state.stack[0] === "string" && !stream.eol()) {
                            if (stream.peek() === state.stringType) {
                                stream.next(); // Skip quote
                                state.stack.shift(); // Clear flag
                            } else if (stream.peek() === "\\") {
                                stream.next();
                                stream.next();
                            } else {
                                stream.match(/^.[^\\\"\']*/);
                            }
                        }
                        return "string";

                    case "symbol":
                        while (state.stack[0] === "symbol" && !stream.eol()) {
                            if (stream.peek() == state.closingBracket) {
                                stream.next(); // Skip closing bracket
                                state.stack.shift(); // Clear symbol context
                            } else {
                                stream.next(); // consume the symbol
                            }
                        }
                        return "variable";
                }

                var peek = stream.peek();

                //no stack
                switch (peek) {
                    case ":":
                        stream.next();
                        return "operator";
                    case "\\":
                    case ".":
                    case ",":
                    case ";":
                    case "*":
                    case "-":
                    case "+":
                    case "^":
                    case "<":
                    case "/":
                    case "=":
                        stream.next();
                        return "atom";
                    case "$":
                        stream.next();
                        return "builtin";
                }

                if (stream.match(/\d+/)) {
                    if (stream.match(/^\w+/)) return "error";
                    return "number";
                } else if (stream.match(/^[a-zA-Z_]\w*/)) {
                    if (stream.match(/(?=[\(.])/, false)) return "keyword";
                    return "variable";
                } else if (["(", ")"].indexOf(peek) != -1) {
                    stream.next();
                    return "bracket";
                } else if (!stream.eatSpace()) {
                    stream.next();
                }
                return null;
            }
        };
    });
});
