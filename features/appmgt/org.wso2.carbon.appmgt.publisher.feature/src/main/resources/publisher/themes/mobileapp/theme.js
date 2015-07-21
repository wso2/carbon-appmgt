//var cache = false;

var engine = caramel.engine('handlebars', (function () {
    return {
        partials: function (Handlebars) {
            var theme = caramel.theme();
            var partials = function (file) {
                (function register(prefix, file) {
                    var i, length, name, files;
                    if (file.isDirectory()) {
                        files = file.listFiles();
                        length = files.length;
                        for (i = 0; i < length; i++) {
                            file = files[i];
                            register(prefix ? prefix + '.' + file.getName() : file.getName(), file);
                        }
                    } else {
                        name = file.getName();
                        if (name.substring(name.length - 4) !== '.hbs') {
                            return;
                        }
                        file.open('r');
                        Handlebars.registerPartial(prefix.substring(0, prefix.length - 4),
                                                   file.readAll());
                        file.close();
                    }
                })('', file);
            };

            partials(new File(theme.resolve('partials')));


            Handlebars.registerHelper('compare', function (lvalue, rvalue, options) {

                if (arguments.length < 3) {
                    throw new Error("Handlerbars Helper 'compare' needs 2 parameters");
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
                    throw new Error("Handlerbars Helper 'compare' doesn't know the operator "
                                    + operator);
                }

                var result = operators[operator](lvalue, rvalue);

                if (result) {
                    return options.fn(this);
                } else {
                    return options.inverse(this);
                }

            });
            Handlebars.registerHelper('ifCond', function (v1, operator, v2, options) {

                switch (operator) {
                    case '==':
                        return (v1 == v2) ? options.fn(this) : options.inverse(this);
                    case '!=':
                        return (v1 != v2) ? options.fn(this) : options.inverse(this);
                    case '===':
                        return (v1 === v2) ? options.fn(this) : options.inverse(this);
                    case '<':
                        return (v1 < v2) ? options.fn(this) : options.inverse(this);
                    case '<=':
                        return (v1 <= v2) ? options.fn(this) : options.inverse(this);
                    case '>':
                        return (v1 > v2) ? options.fn(this) : options.inverse(this);
                    case '>=':
                        return (v1 >= v2) ? options.fn(this) : options.inverse(this);
                    default:
                        return options.inverse(this);
                }
            });

            /*
             * Registers 'for' block helper in Handlebars which mimics classical for-loop.
             * The 'for' helper allows to iterate over an array. Inside the block,
             * you can use 'this' to reference the element being iterated over
             * and 'index' to get the index of the current iteration.
             *
             *
             * Optional Parameters:
             *     startIndex - First index to be iterated over in the loop.
             *                      Should be zero or a positive integer. Default value is 0.
             *     endIndex - Last index to be iterated over in the loop.
             *                      Should be zero or a positive integer. Default value is (data.length - 1).
             *     jump - Increment/decrement of each iteration.
             *                      Should be a positive integer. Default value is 1.
             *
             * Usage:
             *     {{#for items}}
             *         <div>Index of {{this}} value is {{index}}</div>
             *     {{/for}}
             *
             *     {{#for items startIndex="5" endIndex="1"}}
             *         {{this}}
             *     {{/for}}
             *
             *     {{#for items startIndex="2" endIndex="10" jump="2"}}
             *         {{this}}
             *     {{/for}}
             */
            Handlebars.registerHelper('for', function (data, options) {
                if (!Array.isArray(data)) {
                    throw new Error("Handlerbars helper 'for' needs an array to iterate. But found "
                                    + toString.call(data));
                }

                var tmp;
                var startIndex = 0;
                tmp = options.hash['startIndex'];
                if (tmp) {
                    startIndex = parseInt(tmp);
                    if (isNaN(startIndex) || (startIndex < 0)) {
                        throw new Error("'startIndex' parameter of Handlerbars helper 'for' "
                                        + "should be zero or a positive integer. But found " + tmp);
                    }
                }
                var endIndex = data.length - 1;
                tmp = options.hash['endIndex'];
                if (tmp) {
                    endIndex = parseInt(tmp);
                    if (isNaN(endIndex) || (endIndex < 0)) {
                        throw new Error("'endIndex' parameter of Handlerbars helper 'for' "
                                        + "should be zero or a positive integer. But found " + tmp);
                    }
                }
                var jump = 1;
                tmp = options.hash['jump'];
                if (tmp) {
                    jump = parseInt(tmp);
                    if (isNaN(jump) || (jump < 1)) {
                        throw new Error("'jump' parameter of Handlerbars helper 'for' "
                                        + "should be a positive integer. But found " + tmp);
                    }
                }

                var buffer = "";
                if (startIndex <= endIndex) {
                    for (var i = startIndex; i <= endIndex; i += jump) {
                        var item = data[i];
                        if (isObject(item)) {
                            item.index = i;
                        } else {
                            item = new ForLoopItem(item, i);
                        }
                        buffer += options.fn(item);
                    }
                } else {
                    for (var i = startIndex; endIndex <= i; i -= jump) {
                        var item = data[i];
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

            /*
             * Utility function to check given value is an object or not.
             * @return <code>true</code> iff value is an object and
             *          <code>false</code> if value is a string or a number or boolean or an array or a symbol or null or undefined.
             */
            function isObject(value) {
                return (value && ((typeof value) === 'object')
                        && (toString.call(value) === '[object Object]'));
            }

            /*
             * Helper object class for 'for' block helper
             */
            function ForLoopItem(value, index) {
                this.value = value;
                this.index = index;
            }

            ForLoopItem.prototype.toString = function () {
                return String(this.value);
            }

        }
    };
}()));