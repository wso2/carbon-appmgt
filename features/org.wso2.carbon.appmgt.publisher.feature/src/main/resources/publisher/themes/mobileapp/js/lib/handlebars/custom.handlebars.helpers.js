/**
 * Utility function to check given value is an object or not.
 * @param value {*} value to be checked
 * @returns {boolean} <code>true</code> iff value is an object; <code>false</code>
 *     if value is a string or a number or boolean or an array or a symbol or null
 *     or undefined.
 */
function isObject(value) {
    return (value && ((typeof value) === 'object') && (toString.call(value) === '[object Object]'));
}

/**
 * Registers custom Handlebars helpers for the given Handlebars instance.
 * @param handlebarsInstance {Handlebars} Handlebars instance to be registered
 */
var registerHelpers = function (handlebarsInstance) {

    /**
     * Registers 'compare' block helper in Handlebars. This helper acts similar to an if-clause
     * that has a single-expression.
     * @deprecated Use 'ifCond' helper instead of this as 'ifCond' supports more logical operators
     *     and verbose.
     *
     * @param lvalue {Object} left hand side value of the expression
     * @param rvalue {Object} right hand side value of the expression
     *
     * @param {string} [operator=null] operator of the expression; allowed operators are ==, !=,
     *     ===, <,
     *     <=, >, >= and typeof
     *
     * @example
     *          {{compare this.age "18" operator="<="}}
     *              <p>He is a child</p>
     *          {{else}}
     *              <p>He is an adult</p>
     *          {{/compare}}
     *
     *          {{compare item "number" operator="typeof"}}
     *              <p>This is a number</p>
     *          {{else}}
     *              <p>This is not a number</p>
     *          {{/compare}}
     *
     */
    handlebarsInstance.registerHelper('compare', function (lvalue, rvalue, options) {

        if (arguments.length < 3) {
            throw new Error("Handlebars Helper 'compare' needs 2 parameters");
        }

        operator = options.hash.operator || "==";

        var operators = {
            '==': function (l, r) {
                return l == r;
            },
            '===': function (l, r) {
                return l === r;
            },
            '!=': function (l, r) {
                return l != r;
            },
            '<': function (l, r) {
                return l < r;
            },
            '>': function (l, r) {
                return l > r;
            },
            '<=': function (l, r) {
                return l <= r;
            },
            '>=': function (l, r) {
                return l >= r;
            },
            'typeof': function (l, r) {
                return typeof l == r;
            }
        };
        if (!operators[operator]) {
            throw new Error("Handlebars Helper 'compare' doesn't know the operator " + operator);
        }

        var result = operators[operator](lvalue, rvalue);
        if (result) {
            return options.fn(this);
        } else {
            return options.inverse(this);
        }

    });

    /**
     * Registers 'ifCond' block helper in Handlebars. This helper acts similar to an if-clause that
     * has a single-expression.
     *
     * @param lvalue {Object} left hand side value of the expression
     * @param operator {string} operator of the expression; allowed operators are ==, !=, ===, !==,
     *     <, <=, >, >=, &&, || and typeof
     * @param rvalue {Object} right hand side value of the expression
     *
     * @example
     *          {{ifCond this.age "<=" "18"}}
     *              <p>He is a child</p>
     *          {{else}}
     *              <p>He is an adult</p>
     *          {{/ifCond}}
     *
     *          {{ifCond item "typeof" "number"}}
     *              <p>This is a number</p>
     *          {{else}}
     *              <p>This is not a number</p>
     *          {{/ifCond}}
     */
    handlebarsInstance.registerHelper('ifCond', function (lvalue, operator, rvalue, options) {
        switch (operator) {
            case '==':
                return (lvalue == rvalue) ? options.fn(this) : options.inverse(this);
            case '!=':
                return (lvalue != rvalue) ? options.fn(this) : options.inverse(this);
            case '===':
                return (lvalue === rvalue) ? options.fn(this) : options.inverse(this);
            case '!==':
                return (lvalue !== rvalue) ? options.fn(this) : options.inverse(this);
            case '<':
                return (lvalue < rvalue) ? options.fn(this) : options.inverse(this);
            case '<=':
                return (lvalue <= rvalue) ? options.fn(this) : options.inverse(this);
            case '>':
                return (lvalue > rvalue) ? options.fn(this) : options.inverse(this);
            case '>=':
                return (lvalue >= rvalue) ? options.fn(this) : options.inverse(this);
            case '&&':
                return (lvalue && rvalue) ? options.fn(this) : options.inverse(this);
            case '||':
                return (lvalue || rvalue) ? options.fn(this) : options.inverse(this);
            case 'typeof':
                return ((typeof lvalue) == rvalue) ? options.fn(this) : options.inverse(this);
            default:
                throw new Error("Handlebars helper 'ifCond' doesn't know the operator " + operator);
        }
    });

    /**
     * Registers 'for' block helper in Handlebars which mimics classical for-loop.
     * The 'for' helper allows to iterate over an array. Inside the block,
     * you can use 'this' to reference the element being iterated over
     * and 'index' to get the index of the current iteration.
     *
     * @param array {Object[]} array to be iterated over
     *
     * @param {number} [startIndex=0] First index to be iterated over in the loop. Should be zero
     *     or a positive integer. Default value is 0.
     * @param {number} [endIndex=array.length-1] Last index to be iterated over in the loop. Should
     *     be zero or a positive integer. Default value is (array.length - 1).
     * @param {number} [jump=1] Increment/decrement of each iteration. Should be a positive
     *     integer. Default value is 1.
     *
     * @example
     *          {{#for items}}
     *              <div>Index of {{this}} value is {{index}}</div>
     *          {{/for}}
     *
     *          {{#for items startIndex="5" endIndex="1"}}
     *              {{this}}
     *          {{/for}}
     *
     *          {{#for items startIndex="2" endIndex="10" jump="2"}}
     *              {{this}}
     *          {{/for}}
     */
    handlebarsInstance.registerHelper('for', function (array, options) {
        if (!Array.isArray(array)) {
            throw new Error("'for' Handlebars helper needs an array to iterate. But found "
                            + toString.call(array));
        }
        var tmp;
        var startIndex = 0;
        tmp = options.hash['startIndex'];
        if (tmp) {
            startIndex = parseInt(tmp);
            if (isNaN(startIndex) || (startIndex < 0)) {
                throw new Error("'startIndex' parameter of 'for' Handlebars helper "
                                + "should be zero or a positive integer. But found " + tmp);
            }
        }
        var endIndex = array.length - 1;
        tmp = options.hash['endIndex'];
        if (tmp) {
            endIndex = parseInt(tmp);
            if (isNaN(endIndex) || (endIndex < 0)) {
                throw new Error("'endIndex' parameter of 'for' Handlebars helper "
                                + "should be zero or a positive integer. But found " + tmp);
            }
        }
        var jump = 1;
        tmp = options.hash['jump'];
        if (tmp) {
            jump = parseInt(tmp);
            if (isNaN(jump) || (jump < 1)) {
                throw new Error("'jump' parameter of 'for' Handlebars helper "
                                + "should be a positive integer. But found " + tmp);
            }
        }

        /**
         * This is a helper class for the 'for' block helper. This class can hold a given
         * value with the relevant index.
         * @param value {Object} value to be hold
         * @param index {number} index of the iteration
         * @constructor
         */
        function ForLoopItem(value, index) {
            this.value = value;
            this.index = index;
            /**
             * @override
             * @returns {string} string representation of the <code>value</code> of this
             *     object
             */
            this.toString = function () {
                return String(this.value);
            }
        }

        var buffer = "";
        if (startIndex <= endIndex) {
            for (var i = startIndex; i <= endIndex; i += jump) {
                var item = array[i];
                if (isObject(item)) {
                    item.index = i;
                } else {
                    item = new ForLoopItem(item, i);
                }
                buffer += options.fn(item);
            }
        } else {
            for (var i = startIndex; endIndex <= i; i -= jump) {
                var item = array[i];
                if (isObject(item)) {
                    item.index = i;
                } else {
                    item = new ForLoopItem(item, i);
                }
                buffer += options.fn(item);
            }
        }
        return buffer;
    });

    /**
     * Registers 'selectIf' expression helper. This helper evaluates the given <code>value</code>
     * and returns the matching result for that value which is specified in the <code>map</code>.
     *
     *  @param value {Object} value to be evaluated
     *  @param map {Object|string} value-result map; if an Object then {value1: result1, value2:
     *     result2, ... , valuen: resultn}; if string then "value1:result1, value2:result2, ... ,
     *     valuen:resultn"
     *
     *  @example
     *              {{selectIf status colors}}
     *
     *              {{selectIf status "weak:red, normal:blue, great:green"}}
     */
    handlebarsInstance.registerHelper("selectIf", function (value, map) {
        var jsonData = null;
        if ((typeof map) == 'string') {
            if (map.length == 0) {
                throw new Error("An Empty string cannot be passed as the \"value-result\" map "
                                + "for 'selectIf' Handlebars helper.");
            }
            jsonData = {};
            var pairs = map.split(',');
            for (var i = 0; i < pairs.length; i++) {
                var pair = pairs[i];
                var parts = pair.split(':');
                if (parts.length < 2) {
                    throw new Error("Malformed string '" + map + "' passed as the "
                                    + "\"value-result\" map for 'selectIf' Handlebars helper.");
                }
                jsonData[parts[0].trim()] = parts[1].trim();
            }
        } else if (isObject(map)) {
            jsonData = map;
        } else {
            throw new Error("Handlebars helper 'selectIf' needs an Object or a well-formed string "
                            + "as the \"value-result\" map. But found " + toString.call(map));
        }
        var key = String(value).trim();
        return new handlebarsInstance.SafeString(String(jsonData[key]));
    });

};