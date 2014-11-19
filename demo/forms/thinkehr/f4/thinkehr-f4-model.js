/**
 * Created by matijak on 6.8.2014.
 */

// Namespace definitions

if (!thinkehr) {
    var thinkehr = {};
}
if (!thinkehr.f4) {
    thinkehr.f4 = {};
    thinkehr.f4.PREFIX = "ehr-";
}
if (!thinkehr.f4.util) {
    thinkehr.f4.util = {};
}

(function namespace() {

    // This code is a sub-classing routine from the JavaScript Ninja book, Chapter 6, listing 21

    //noinspection JSCheckFunctionSignatures
    var initializing = false,
        superPattern =  // Determine if functions can be serialized
            /xyz/.test(function () {
                //noinspection BadExpressionStatementJS,JSUnresolvedVariable
                xyz;
            }) ? /\b_super\b/ : /.*/;       //#1

    // Creates a new Class that inherits from this class
    Object._extendM = function (properties) {                           //#2
        var _super = this.prototype;

        // Instantiate a base class (but only create the instance,
        // don't run the init constructor)
        initializing = true;                                              //#3
        var proto = new this();                                           //#3
        initializing = false;                                             //#3

        // Copy the properties over onto the new prototype
        for (var name in properties) {                                    //#4
            // Check if we're overwriting an existing function
            //noinspection JSUnfilteredForInLoop
            proto[name] = typeof properties[name] == "function" &&
                typeof _super[name] == "function" &&
                superPattern.test(properties[name]) ?
                (function (name, fn) {                                        //#5
                    return function () {
                        var tmp = this._super;

                        // Add a new ._super() method that is the same method
                        // but on the super-class
                        //noinspection JSUnfilteredForInLoop
                        this._super = _super[name];

                        // The method only need to be bound temporarily, so we
                        // remove it when we're done executing
                        var ret = fn.apply(this, arguments);
                        this._super = tmp;

                        return ret;
                    };
                })(name, properties[name]) :
                properties[name];
        }

        // The dummy class constructor
        function Class() {                                                   //#6
            // All construction is actually done in the init method
            //noinspection JSUnresolvedVariable
            if (!initializing && this.init) { //noinspection JSUnresolvedVariable
                this.init.apply(this, arguments);
            }
        }

        // Populate our constructed prototype object
        Class.prototype = proto;                                             //#7

        // Enforce the constructor to be what we expect
        Class.constructor = Class;                                           //#8

        // And make this class extension-capable
        Class._extendM = arguments.callee;                                   //#9

        return Class;
    };


    // enumerations
    thinkehr.f4.enumeration = function (namesToValues) {
        // Constructor, just throws an error since enumerations can't be instantiated
        var enumeration = function () {
            throw "Can't instantiate enumerations";
        };

        var proto = enumeration.prototype = {
            constructor: enumeration,

            toString: function () {
                return this.name;
            },

            toJSON: function () {
                return this.name;
            },

            valueOf: function () {
                return this.value;
            }
        };

        enumeration.values = [];

        for (var name in namesToValues) {
            var e = {};
            for (var prop in proto) {
                //noinspection JSUnfilteredForInLoop
                e[prop] = proto[prop];
            }
            e.name = name;
            //noinspection JSUnfilteredForInLoop
            e.value = namesToValues[name];
            //noinspection JSUnfilteredForInLoop
            enumeration[name] = e;
            enumeration.values.push(e);
        }

        enumeration.foreach = function (f, c) {
            for (var i = 0; i < this.values.length; i++) {
                f.call(c, this.values[i]);
            }
        };

        enumeration.fromString = function (str) {
            if (!str) {
                return null;
            }

            var strLc = str.toLowerCase();

            for (var i = 0; i < this.values.length; i++) {
                var e = this.values[i];
                if (strLc === e.name.toLowerCase()) {
                    return e;
                }
            }

            return str;
        };

        return enumeration;
    };

    // extend
    thinkehr.f4._extend = function (from, to) {
        for (var prop in from) {
            if (from.hasOwnProperty(prop)) {
                to[prop] = from[prop];
            }
        }
    };


    // ViewConfig making------------------------------------------------------------------------------------------------------------------------

    function _parseSizeViewConfig(viewConfig, sizeJson) {
        var size = new thinkehr.f4.Size({viewConfig: viewConfig});
        for (var prop in sizeJson) {
            if (sizeJson.hasOwnProperty(prop)) {
                if (prop === "field") {
                    size.setField(thinkehr.f4.FieldSize.fromString(sizeJson[prop]));
                } else if (prop === "label") {
                    var lbl = parseInt(sizeJson[prop]);
                    size.setLabel(isNaN(lbl) ? thinkehr.f4.LabelSize.fromString(sizeJson[prop]) : lbl);
                } else {
                    size[prop] = sizeJson[prop];
                }
            }
        }

        return size;
    }

    function _parseLayoutViewConfig(viewConfig, layoutJson) {
        var layout = new thinkehr.f4.Layout({viewConfig: viewConfig});
        for (var prop in layoutJson) {
            if (layoutJson.hasOwnProperty(prop)) {
                if (prop === "field") {
                    if (layoutJson[prop]["align"]) {
                        layout.setFieldHorizontalAlignment(thinkehr.f4.FieldHorizontalAlignment.fromString(layoutJson[prop]["align"]));
                    }
                    if (layoutJson[prop]["valign"]) {
                        layout.setFieldVerticalAlignment(thinkehr.f4.FieldVerticalAlignment.fromString(layoutJson[prop]["valign"]));
                    }
                    _addPropsToObject(layoutJson[prop], layout, "field", ["align", "valign"]);
                } else if (prop === "label") {
                    if (layoutJson[prop]["align"]) {
                        layout.setLabelHorizontalAlignment(thinkehr.f4.LabelHorizontalAlignment.fromString(layoutJson[prop]["align"]));
                    }
                    if (layoutJson[prop]["valign"]) {
                        layout.setLabelVerticalAlignment(thinkehr.f4.LabelVerticalAlignment.fromString(layoutJson[prop]["valign"]));
                    }
                    _addPropsToObject(layoutJson[prop], layout, "label", ["align", "valign"]);
                } else {
                    layout[prop] = layoutJson[prop];
                }
            }
        }

        return layout;
    }

    function _parseLabelViewConfig(viewConfig, labelJson) {
        var label = new thinkehr.f4.Label({viewConfig: viewConfig});
        thinkehr.f4._extend(labelJson, label);

        return label;
    }

    function _parseFieldViewConfig(viewConfig, fieldJson) {
        var field = new thinkehr.f4.Field({viewConfig: viewConfig});
        thinkehr.f4._extend(fieldJson, field);

        if (fieldJson.presentation) {
            field.setPresentation(thinkehr.f4.FieldPresentation.fromString(fieldJson.presentation));
        }

        if (fieldJson.columns) {
            var columns = parseInt(fieldJson.columns);
            if (!isNaN(columns)) {
                field.setColumns(columns);
            }
        }

        if (fieldJson.lines) {
            var lines = parseInt(fieldJson.lines);
            if (!isNaN(lines)) {
                field.setLines(lines);
            }
        }

        return field;
    }

    function _parseAdvancedViewConfig(viewConfig, advancedJson) {
        _addPropsToObject(advancedJson, viewConfig, "advanced", ["hidden", "readonly"]);

        if (advancedJson.hidden) {
            viewConfig.setHidden(advancedJson.hidden);
        }

        if (advancedJson.readonly) {
            viewConfig.setReadOnly(advancedJson.readonly);
        }

        return null;
    }

    function _parseMultiplicityViewConfig(viewConfig, multiplicityJson) {
        _addPropsToObject(multiplicityJson, viewConfig, "multiplicity", ["min", "max"]);

        if (multiplicityJson.min) {
            var min = parseInt(multiplicityJson.min);
            viewConfig.setMin(isNaN(min) ? multiplicityJson.min : min);
        }

        if (multiplicityJson.max) {
            var max = parseInt(multiplicityJson.max);
            viewConfig.setMax(isNaN(max) ? multiplicityJson.max : max);
        }

        return null;
    }

    function _addPropsToObject(from, to, toPropertyName, skip) {
        var added = 0;
        var tmpAdd = {};
        for (var prop in from) {
            if (from.hasOwnProperty(prop) && skip.indexOf(prop) < 0) {
                tmpAdd[prop] = from[prop];
                added++;
            }
        }

        if (added > 0) {
            to[toPropertyName] = tmpAdd;
        }
    }

    var _knownViewConfigProps = {
        "size": _parseSizeViewConfig,
        "layout": _parseLayoutViewConfig,
        "label": _parseLabelViewConfig,
        "field": _parseFieldViewConfig,
        "advanced": _parseAdvancedViewConfig,
        "multiplicity": _parseMultiplicityViewConfig
    };


    function parseViewConfig(viewConfigPlain) {

        var vc = new thinkehr.f4.ViewConfig();

        for (var prop in viewConfigPlain) {
            if (viewConfigPlain.hasOwnProperty(prop)) {
                var makerFunction = _knownViewConfigProps[prop];
                if (makerFunction) {
                    var propToAdd = makerFunction(vc, viewConfigPlain[prop]);
                    if (propToAdd) {
                        vc[prop] = propToAdd;
                    }
                }
                else {
                    vc[prop] = viewConfigPlain[prop];
                }
            }
        }

        return vc;
    }

    //--------------Parsing-------------------------------------------------------------------------------------------------------------------------------------

    function parseFormDescription(context, formDescription, values) {
        var rootRmType = thinkehr.f4.RmType.fromString(formDescription.rmType);

        if (rootRmType !== thinkehr.f4.RmType.FORM_DEFINITION) {
            throw new Error("Root element is not form definition"); // Using this syntax cause of Jasmine
        }

        var rootObj = new thinkehr.f4.FormRootModel(formDescription);
        rootObj.setDescEl(formDescription);
        if (rootObj.children !== undefined) {
            delete  rootObj.children;
        }
        _parseViewConfig(rootObj, formDescription);
        if (formDescription.children) {
            for (var i = 0; i < formDescription.children.length; i++) {
                _recursivelyParseDescription(rootObj, context, formDescription.children[i]);
            }
        }

        if (!values) {
            values = {};
        }
        rootObj.setValueNodeRef(values);
        _parseValues(rootObj, values, values);

        return rootObj;
    }

    function parseFormDescriptionSnippet(snippet, values) {
        var model = _recursivelyParseDescription(null, {}, snippet);
        if (model) {
            if (!values) {
                values = {};
            }
            _parseValues(model, values, values);
        }

        return model;
    }

    function _recursivelyParseDescription(parentModel, context, descEl) {
        var model = _parseRmClassCreateObj(descEl);
        if (model != null) {
            if (parentModel) {
                parentModel.addChildModel(model);
            }
            _parseViewConfig(model, descEl);
            _parseInputs(model, descEl);
            if (descEl.children) {
                for (var i = 0; i < descEl.children.length; i++) {
                    _recursivelyParseDescription(model, context, descEl.children[i]);
                }
            }
        } else {
            console.warn("Skipping model creation for element: ", descEl);
        }

        return model;
    }

    function _parseRmClassCreateObj(descEl) {
        var cl = thinkehr.f4._rmTypeToClassMap[descEl.rmType];
        var rmType;
        if (!cl) {
            if (_isContainerDesc(descEl)) {
                cl = thinkehr.f4.RmContainerModel;
                rmType = thinkehr.f4.RmType.fromString(descEl.rmType);
            } else {
                console.warn("Unknown rmType: ", descEl.rmType, " skipping model creation");
                return null;
            }
        } else {
            rmType = null;
        }

        var obj = new cl(descEl);
        obj.setDescEl(descEl);
        if (rmType) {
            obj.setRmType(rmType);
        }
        if (obj.children !== undefined) {
            delete obj.children;
        }

        return obj;
    }

    function _isContainerDesc(descEl) {
        return descEl.children !== undefined && thinkehr.f4.util.isArray(descEl.children) && descEl.rmType;
    }

    function _parseViewConfig(model, descEl) {
        var vc;
        if (descEl.viewConfig) {
            vc = parseViewConfig(descEl.viewConfig);
        } else {
            console.warn("Warning, no view config found for element ", descEl, ", creating an empty one for hierarchy purposes");
            vc = new thinkehr.f4.ViewConfig();
        }

        model.setViewConfig(vc);
        vc.setModel(model);

        return vc;
    }

    function _parseInputs(model, descEl) {
        if (descEl.inputs && model instanceof thinkehr.f4.NodeModel) {
            model.setInputs([]);
            for (var i = 0; i < descEl.inputs.length; i++) {
                var input = descEl.inputs[i];
                var inputObj = new thinkehr.f4.Input(input);
                inputObj.setList([]);

                inputObj.setType(thinkehr.f4.InputType.fromString(input.type));

                if (input.list) {
                    for (var j = 0; j < input.list.length; j++) {
                        var inputItem = input.list[j];
                        var inputItemObj = new thinkehr.f4.InputItem(inputItem);
                        if (inputItem.validation) {
                            inputItemObj.setValidation(new thinkehr.f4.Validation(inputItem.validation));
                        }
                        inputObj.addItem(inputItemObj);
                    }
                }

                model.addInput(inputObj);
            }
        }
    }

    function _parseValues(model, values, rootValues) {
        var models = [];
        _getModelsRecursively(model, models);
        for (var i = 0; i < models.length; i++) {
            var m = models[i];
            var path = m.getPath();
            if (path != null && m.isAttachableToValueNode() && m.getRmType() !== thinkehr.f4.RmType.FORM_DEFINITION) {
                var segments = path.split("/");
                _attachValueToModels(m, segments, 0, values, rootValues);
            }
        }
    }

    function recursivelyForChildModels(model, func) {
        for (var i = 0; i < model.getChildCount(); i++) {
            var childModel = model.getChildModel(i);
            func(childModel);
            recursivelyForChildModels(childModel, func);
        }
    }

    function _getPathChildIndex(model) {
        if (model.getParentModel() && model.getPath()) {
            var allChildrenWithPath = model.getParentModel().findChildrenWithPath(model.getPath());
            return allChildrenWithPath.indexOf(model);
        }

        return -1;
    }

    function _markModelForDeletion(model) {
        model.__markedForDeletion = true;
        recursivelyForChildModels(model, function (childModel) {
            childModel.__markedForDeletion = true;
        });
        model.removeFromParent();
    }

    function _attachValueToModels(model, segments, segmentIndex, values, rootValues) {
        if (!values || segmentIndex >= segments.length || model.__markedForDeletion === true) {
            return;
        }

        var segment = segments[segmentIndex];

        var lastSegment = segments.length === segmentIndex + 1;
        var segmentPath = _segmentsToPath(segments, segmentIndex);

        var addedSegment = false;
        var pathChildIndex = -1;
        if (values[segment] === undefined) {
            pathChildIndex = _getPathChildIndex(model);
            if (pathChildIndex > 0) {
                _markModelForDeletion(model);
            } else {
                values[segment] = values === rootValues ? {} : [];
                console.info("Creating empty value for ", segmentPath, ":", values[segment]);
                addedSegment = true;
            }
        }

        if (values[segment] !== undefined) {
            var node = values[segment];

            var val;
            var valRef;
            var parentValRef;

            if (!lastSegment) {
                var ancestorForSegment = model.findAncestorWithPath(segmentPath);
                // We've already handled any splits further up the tree
                if (ancestorForSegment) {
                    _attachValueToModels(model, segments, segmentIndex + 1, ancestorForSegment.getValueNodeRef(), rootValues);
                    return;
                }

                val = null;
                parentValRef = null;
                if (thinkehr.f4.util.isArray(node)) {
                    if (node.length > 1) {
                        console.warn("A split occurred in the model at", segmentPath, " will only use last value from the array for " +
                            "further processing");
                    }
                    if (addedSegment) {
                        valRef = {};
                        node.push(valRef);
                    } else {
                        valRef = node.length === 0 ? null : node[node.length - 1];
                    }
                } else {
                    valRef = node;
                }
            } else {
                // We are at our model

                if (pathChildIndex < 0) {
                    pathChildIndex = _getPathChildIndex(model);
                }

                if (pathChildIndex > 0) {
                    if (!thinkehr.f4.util.isArray(node) || pathChildIndex >= node.length) {
                        _markModelForDeletion(model);
                    } else {
                        parentValRef = node;
                        valRef = node[pathChildIndex];
                        val = node[pathChildIndex];
                    }
                } else if (thinkehr.f4.util.isArray(node)) {

                    if (node.length === 0) {
                        val = null;
                        if (addedSegment && !model.isValueModel()) {
                            valRef = {};
                            node.push(valRef);
                        } else {
                            valRef = null;
                        }
                        parentValRef = node;
                    } else if (node.length === 1) {
                        val = node[0];
                        valRef = node[0];
                        parentValRef = node;
                    } else {
                        if (model.isValueModel() && !model.isMulti()) {
                            console.warn("Multiple values received for non-multi model", segmentPath, ", will only use the last one.");
                            val = node[node.length - 1];
                            valRef = val;
                            parentValRef = node;
                        } else {
                            val = node;
                            valRef = node;
                            parentValRef = values;
                        }

                        // Do the splits
                        if (!model.isValueModel()) {

                            if (segmentPath != model.getPath()) {
                                // Should never happen, this is a programmer assertion
                                throw new Error("Segment path not the same as model path when we want to possibly split the model. Coding error?");
                            }
                            var parentModel = model.getParentModel();
                            for (var i = 1; i < valRef.length; i++) {
                                // Do we already have our child?
                                var childModel = parentModel.findChildWithPath(segmentPath, i);
                                if (!childModel) {
                                    _recursivelyParseDescription(parentModel, {}, model.getDescEl());
                                    childModel = parentModel.findChildWithPath(segmentPath, i);
                                    if (!childModel) {
                                        throw Error("Could not create child model for", model.getPath(), "."); // Should REALLY never happen
                                    }
                                }

                                childModel.setValueNodeRef(valRef[i]);
                                childModel.setValueNodeParentRef(valRef);

                                var subModels = [];
                                _getModelsRecursively(childModel, subModels);
                                subModels.shift(); // Remove the childModel itself, since it's already been handled
                                for (var j = 0; j < subModels.length; j++) {
                                    var subModel = subModels[j];
                                    if (subModel.getPath() != null) {
                                        var subModelSegments = subModel.getPath().split("/");
                                        _attachValueToModels(subModel, subModelSegments, 0, rootValues, rootValues);
                                    }
                                }
                            }

                            // This is for the first element, they will be set below
                            parentValRef = valRef;
                            valRef = valRef[0];
                        } // splits
                    } // array with multiple elements

                } // is array ends here
                else {
                    // object
                    val = node;
                    valRef = node;
                    parentValRef = values;
                }

                if (model.isValueModel()) {
                    model.setValue(val);
                }

                model.setValueNodeRef(valRef);
                model.setValueNodeParentRef(parentValRef);
                if (!parentValRef && model.__markedForDeletion !== true) {
                    console.warn("Parent value reference is null for", model, ". Is this legal?");
                }

            } // is last segment

            _attachValueToModels(model, segments, segmentIndex + 1, valRef, rootValues);

        } // we have some values
    }

    function _segmentsToPath(segments, lastIndex) {
        var index = lastIndex === undefined ? segments.length : lastIndex + 1;
        var sliced = segments.slice(0, index);
        return sliced.join("/");
    }

    function refreshValues(model, values) {
        if (model.getRmType() === thinkehr.f4.RmType.FORM_DEFINITION) {
            model.setValueNodeRef(values);
        }
        _parseValues(model, values, values);

        model._triggerModelRefreshed();
        recursivelyForChildModels(model, function (m) {
            m._triggerModelRefreshed();
        })
    }

    function _getModelsRecursively(model, flatArray) {
        flatArray.push(model);
        for (var i = 0; i < model.getChildCount(); i++) {
            var childModel = model.getChildModel(i);
            _getModelsRecursively(childModel, flatArray);
        }
    }

    function duplicateModel(model, insertAfterModel) {
        var values = {};
        var newModel = parseFormDescriptionSnippet(model.getDescEl(), values);

        if (insertAfterModel) {
            var parent = model.getParentModel();
            var index;
            if (parent) {
                index = parent.getChildModels().indexOf(insertAfterModel);
                if (parent) {
                    if (index >= 0) {
                        parent.insertChildModelAt(index + 1, newModel);
                    }
                }
            }

            var parentValRef = model.getValueNodeParentRef();
            if (parentValRef && thinkehr.f4.util.isArray(parentValRef)) {
                newModel.setValueNodeParentRef(parentValRef);
                if (index === undefined || index < 0) {
                    parentValRef.push(newModel.getValueNodeRef());
                } else {
                    parentValRef.splice(index + 1, 0, newModel.getValueNodeRef());
                }
            }
        }

        return newModel;
    }

    function destroyModel(model) {
        model.removeFromParent();

        var parentValRef = model.getValueNodeParentRef();
        var valRef = model.getValueNodeRef();
        if (parentValRef && valRef) {
            if (thinkehr.f4.util.isArray(parentValRef)) {
                var index = parentValRef.indexOf(valRef);
                if (index >= 0) {
                    parentValRef.splice(index, 1);
                }
            }
        }
    }

    //  Util
    function isArray(o) {
        return Object.prototype.toString.call(o) === '[object Array]';
    }

    function isObject(o) {
        return Object.prototype.toString.call(o) === '[object Object]';
    }

    function isString(o) {
        return Object.prototype.toString.call(o) === '[object String]';
    }

    function isInteger(o) {
        return (o === parseInt(o));
    }

    function deepClone(o) {
        return JSON.parse(JSON.stringify(o));
    }

    thinkehr.f4.util.isArray = isArray;
    thinkehr.f4.util.isObject = isObject;
    thinkehr.f4.util.isString = isString;
    thinkehr.f4.util.isInteger = isInteger;
    thinkehr.f4.util.deepClone = deepClone;

    // Exports
    thinkehr.f4.parseViewConfig = parseViewConfig;
    thinkehr.f4.parseFormDescription = parseFormDescription;
    thinkehr.f4.parseFormDescriptionSnippet = parseFormDescriptionSnippet;
    thinkehr.f4.duplicateModel = duplicateModel;
    thinkehr.f4.destroyModel = destroyModel;
    thinkehr.f4.refreshValues = refreshValues;
    thinkehr.f4.recursivelyForChildModels = recursivelyForChildModels;
})();

//---Enumerations-----------------------------------------------------------------------------------------------------------------------------------------------

// RM Types enumeration
thinkehr.f4.RmType = thinkehr.f4.enumeration({
    FORM_DEFINITION: 0,
    GENERIC_FIELDSET: 1,
    DV_QUANTITY: 2,
    DV_CODED_TEXT: 3,
    DV_TEXT: 4,
    DV_PROPORTION: 5,
    DV_BOOLEAN: 6,
    DV_DATE: 7,
    DV_TIME: 8,
    DV_DATE_TIME: 9,
    DV_ORDINAL: 10,
    OBSERVATION: 1001,
    EVENT: 1002,
    COMPOSITION: 1003,
    SECTION: 1004
});

// FieldSize
thinkehr.f4.FieldSize = thinkehr.f4.enumeration({
    INHERIT: 0,
    SMALL: 1,
    MEDIUM: 2,
    LARGE: 3
});

thinkehr.f4.LabelSize = thinkehr.f4.enumeration({
    INHERIT: 0
});

// Field alignment
thinkehr.f4.FieldHorizontalAlignment = thinkehr.f4.enumeration({
    INHERIT: 0,
    LEFT: 1,
    CENTER: 2,
    RIGHT: 3
});

thinkehr.f4.FieldVerticalAlignment = thinkehr.f4.enumeration({
    INHERIT: 0,
    TOP: 1,
    MIDDLE: 2,
    BOTTOM: 3
});

// Label alignment
thinkehr.f4.LabelHorizontalAlignment = thinkehr.f4.enumeration({
    INHERIT: 0,
    TOP: 1,
    LEFT: 2,
    RIGHT: 3
});

thinkehr.f4.LabelVerticalAlignment = thinkehr.f4.enumeration({
    INHERIT: 0,
    TOP: 1,
    MIDDLE: 2,
    BOTTOM: 3
});

thinkehr.f4.InputType = thinkehr.f4.enumeration({
    DECIMAL: 0,
    CODED_TEXT: 1,
    TEXT: 2,
    INTEGER: 3,
    BOOLEAN: 4,
    DATE: 5,
    TIME: 6,
    DATETIME: 7
});

thinkehr.f4.FieldPresentation = thinkehr.f4.enumeration({
    COMBOBOX: 0,
    RADIOS: 1,
    TEXTFIELD: 2,
    TEXTAREA: 3
});

//---Model Properties--------------------------------------------------------------------------------------------------------------------------------------------

thinkehr.f4.Object = Object._extendM({
    init: function (properties) {
        if (properties) {
            for (var prop in properties) {
                if (properties.hasOwnProperty(prop)) {
                    this[prop] = properties[prop];
                }
            }
        }
    },

    /*
     * @Override
     */
    toString: function () {
        return "thinkehr.f4.Object";
    }
});

thinkehr.f4.ViewConfig = thinkehr.f4.Object._extendM({
    init: function (properties) {
        this._super(properties);

        if (this.model === undefined) {
            this.model = null;
        }
        if (this.label === undefined) {
            this.label = null;
        }
        if (this.size === undefined) {
            this.size = null;
        }
        if (this.layout === undefined) {
            this.layout = null;
        }
        if (this.field === undefined) {
            this.field = null;
        }
        if (this.hidden === undefined) {
            this.hidden = false;
        }
        if (this.readonly === undefined) {
            this.readonly = false;
        }
        if (this.min === undefined) {
            this.min = null;
        }
        if (this.max === undefined) {
            this.max = null
        }
    },

    getModel: function () {
        return this.model;
    },

    setModel: function (model) {
        this.model = model;
    },

    getLabel: function () {
        return this.label;
    },

    setLabel: function (label) {
        this.label = label;
    },

    getSize: function (hierarchy) {
        return this._getHierarchicalViewConfigProperty(hierarchy, "size", this.getSize)
    },

    setSize: function (size) {
        this.size = size;
    },

    getLayout: function (hierarchy) {
        return this._getHierarchicalViewConfigProperty(hierarchy, "layout", this.getLayout);
    },

    setLayout: function (layout) {
        this.layout = layout;
    },

    getField: function () {
        return this.field;
    },

    setField: function (field) {
        this.field = field;
    },

    isHidden: function () {
        return this.hidden;
    },

    setHidden: function (hidden) {
        this.hidden = hidden;
    },

    isReadOnly: function () {
        return this.readonly;
    },

    setReadOnly: function (readonly) {
        this.readonly = readonly;
    },

    getMin: function (hierarchy) {
        return this._getModelPropertyIfNotOwn(hierarchy, "min", "getMin");
    },

    setMin: function (min) {
        this.min = min;
    },

    getMax: function (hierarchy) {
        return this._getModelPropertyIfNotOwn(hierarchy, "max", "getMax");
    },

    setMax: function (max) {
        this.max = max;
    },

    _getModelPropertyIfNotOwn: function (hierarchy, property, getterProperty) {
        if (hierarchy === false || this[property] != null) {
            return this[property];
        }

        if (this.model && this.model[getterProperty]) {
            return this.model[getterProperty].call(this.model);
        }

        return null;
    },

    _getHierarchicalViewConfigProperty: function (hierarchy, property, getter) {
        if (hierarchy === false || this[property] != null) {
            return this[property];
        }
        if (this.model != null && this.model.getParentModel()) {
            return getter.call(this.model.getParentModel().getViewConfig(), true);
        }

        return null;
    },

    /*
     * @Override
     */
    toString: function () {
        return "thinkehr.f4.ViewConfig";
    }
});

thinkehr.f4.ViewConfigProperty = thinkehr.f4.Object._extendM({
    init: function (properties) {
        this._super(properties);
        if (this.viewConfig === undefined) {
            this.viewConfig = null;
        }
    },

    getViewConfig: function () {
        return this.viewConfig;
    },
    setViewConfig: function (viewConfig) {
        this.viewConfig = viewConfig;
    },

    /**
     * Calls a hierarchical property, sort of reflectively up the chain.
     *
     */
    _getHierarchicalProperty: function (hierarchy, propKey, inheritValue, fieldGetter, containingObjGetterProp) {
        if (hierarchy === false || (this[propKey] && this[propKey] != inheritValue)) {
            return this[propKey];
        }

        var containingObjGetter = this._getParentViewConfig() ? this._getParentViewConfig()[containingObjGetterProp] : null;
        if (containingObjGetter && containingObjGetter.call(this._getParentViewConfig(), true)) {
            return fieldGetter.call(containingObjGetter.call(this._getParentViewConfig(), true), true);
        }

        return this[propKey];
    },

    _getParentViewConfig: function () {
        if (this.viewConfig.getModel() && this.viewConfig.getModel().getParentModel() &&
            this.viewConfig.getModel().getParentModel() instanceof thinkehr.f4.FormObjectModel) {
            return this.viewConfig.getModel().getParentModel().getViewConfig();
        }

        return null;
    },

    /*
     * @Override
     */
    toString: function () {
        return "thinkehr.f4.ViewConfigProperty";
    }
});

thinkehr.f4.Size = thinkehr.f4.ViewConfigProperty._extendM({
    init: function (properties) {
        this._super(properties);
        if (this.field === undefined) {
            this.field = null;
        }
        if (this.label === undefined) {
            this.label = null;
        }
    },

    getField: function (hierarchy) {
        return this._getHierarchicalProperty(hierarchy, "field", thinkehr.f4.FieldSize.INHERIT, this.getField, "getSize");
    },

    setField: function (field) {
        this.field = field;
    },

    getLabel: function (hierarchy) {
        return this._getHierarchicalProperty(hierarchy, "label", thinkehr.f4.LabelSize.INHERIT, this.getLabel, "getSize");
    },

    setLabel: function (label) {
        this.label = label;
    },

    /*
     * @Override
     */
    toString: function () {
        return "thinkehr.f4.Size/ViewConfigProperty";
    }
});

thinkehr.f4.Layout = thinkehr.f4.ViewConfigProperty._extendM({
    init: function (properties) {
        this._super(properties);
        if (this.viewConfig === undefined) {
            this.viewConfig = null;
        }
        if (this.fieldHorizontalAlignment === undefined) {
            this.fieldHorizontalAlignment = null;
        }
        if (this.fieldVerticalAlignment === undefined) {
            this.fieldVerticalAlignment = null;
        }
        if (this.labelHorizontalAlignment === undefined) {
            this.labelHorizontalAlignment = null;
        }
        if (this.labelVerticalAlignment === undefined) {
            this.labelVerticalAlignment = null;
        }
    },

    getFieldHorizontalAlignment: function (hierarchy) {
        return this._getHierarchicalProperty(
            hierarchy,
            "fieldHorizontalAlignment",
            thinkehr.f4.FieldHorizontalAlignment.INHERIT,
            this.getFieldHorizontalAlignment,
            "getLayout"
        );
    },

    setFieldHorizontalAlignment: function (alignment) {
        this.fieldHorizontalAlignment = alignment;
    },

    getFieldVerticalAlignment: function (hierarchy) {
        return this._getHierarchicalProperty(
            hierarchy,
            "fieldVerticalAlignment",
            thinkehr.f4.FieldVerticalAlignment.INHERIT,
            this.getFieldVerticalAlignment,
            "getLayout"
        );
    },

    setFieldVerticalAlignment: function (alignment) {
        this.fieldVerticalAlignment = alignment;
    },

    getLabelHorizontalAlignment: function (hierarchy) {
        return this._getHierarchicalProperty(
            hierarchy,
            "labelHorizontalAlignment",
            thinkehr.f4.LabelHorizontalAlignment.INHERIT,
            this.getLabelHorizontalAlignment,
            "getLayout"
        );
    },

    setLabelHorizontalAlignment: function (alignment) {
        this.labelHorizontalAlignment = alignment;
    },

    getLabelVerticalAlignment: function (hierarchy) {
        return this._getHierarchicalProperty(
            hierarchy,
            "labelVerticalAlignment",
            thinkehr.f4.LabelVerticalAlignment.INHERIT,
            this.getLabelVerticalAlignment,
            "getLayout"
        );
    },

    setLabelVerticalAlignment: function (alignment) {
        this.labelVerticalAlignment = alignment;
    },

    /*
     * @Override
     */
    toString: function () {
        return "thinkehr.f4.Layout/ViewConfigProperty";
    }
});

thinkehr.f4.Label = thinkehr.f4.ViewConfigProperty._extendM({
    init: function (properties) {
        this._super(properties);

        if (this.custom === undefined) {
            this.custom = false;
        }

        if (this.value === undefined) {
            this.value = null;
        }

        if (this.useLocalizations === undefined) {
            this.useLocalizations = false;
        }

        if (this.localizationsList === undefined) {
            this.localizationsList = {};
        }
    },

    isCustom: function () {
        return this.custom;
    },

    setCustom: function (custom) {
        this.custom = custom;
    },

    getValue: function () {
        return this.value;
    },

    setValue: function (value) {
        this.value = value;
    },

    isUseLocalizations: function () {
        return this.useLocalizations;
    },

    setUseLocalizations: function (useLocalizations) {
        this.useLocalizations = useLocalizations;
    },

    getLocalization: function (locale) {
        var l = this.localizationsList[locale];
        return l ? l : null;
    },

    setLocalizations: function (localizationsList) {
        this.localizationsList = localizationsList;
    },

    /*
     * @Override
     */
    toString: function () {
        return "thinkehr.f4.Label/ViewConfigProperty";
    }
});

thinkehr.f4.Field = thinkehr.f4.ViewConfigProperty._extendM({
    init: function (properties) {
        this._super(properties);

        if (this.presentation === undefined) {
            this.presentation = null;
        }
        if (this.columns === undefined) {
            this.columns = null;
        }
        if (this.lines === undefined) {
            this.lines = null;
        }
    },

    getPresentation: function () {
        return this.presentation;
    },

    setPresentation: function (presentation) {
        this.presentation = presentation;
    },

    getColumns: function () {
        return this.columns;
    },

    setColumns: function (columns) {
        this.columns = columns;
    },

    getLines: function () {
        return this.lines;
    },

    setLines: function (lines) {
        this.lines = lines;
    },

    /*
     * @Override
     */
    toString: function () {
        return "thinkehr.f4.Field/ViewConfigProperty";
    }
});

thinkehr.f4.Input = thinkehr.f4.Object._extendM({
    init: function (properties) {
        this._super(properties);

        if (this.suffix === undefined) {
            this.suffix = null;
        }

        if (this.type === undefined) {
            this.type = null;
        }

        if (this.list === undefined) {
            this.list = [];
        }

        if (this.validation === undefined) {
            this.validation = null;
        }
    },

    getSuffix: function () {
        return this.suffix;
    },

    setSuffix: function (suffix) {
        this.suffix = suffix;
    },

    getType: function () {
        return this.type;
    },

    setType: function (type) {
        this.type = type;
    },

    getList: function () {
        return this.list;
    },

    setList: function (list) {
        this.list = list;
    },

    getItem: function (index) {
        return index >= 0 && index < this.list.length ? this.list[index] : null;
    },

    addItem: function (item) {
        this.list.push(item);
    },

    hasItems: function () {
        return this.list.length > 0;
    },

    getValidation: function () {
        return this.validation;
    },

    setValidation: function (validation) {
        this.validation = validation;
    },

    /*
     * @Override
     */
    toString: function () {
        return "thinkehr.f4.Input";
    }
});

thinkehr.f4.InputItem = thinkehr.f4.Object._extendM({
    init: function (properties) {
        this._super(properties);

        if (this.value === undefined) {
            this.value = null;
        }

        if (this.label === undefined) {
            this.label = null;
        }

        if (this.validation === undefined) {
            this.validation = null;
        }

        if (this.localizedLabels === undefined) {
            this.localizedLabels = {};
        }
    },

    getValue: function () {
        return this.value;
    },

    setValue: function (value) {
        this.value = value;
    },

    getLabel: function (locale) {
        if (locale) {
            var l = this.localizedLabels[locale];
            return l ? l : this.label;
        }

        return this.label;
    },

    setLabel: function (label) {
        this.label = label;
    },

    getValidation: function () {
        return this.validation;
    },

    setValidation: function (validation) {
        this.validation = validation;
    },

    setLocalizedLabels: function (localizedLabels) {
        this.localizedLabels = localizedLabels;
    },

    /*
     * @Override
     */
    toString: function () {
        return "thinkehr.f4.InputItem";
    }
});

thinkehr.f4.Validation = thinkehr.f4.Object._extendM({
    init: function (properties) {
        this._super(properties);

        if (this.precision === undefined) {
            this.precision = null;
        }
    },

    getPrecision: function () {
        return this.precision;
    },

    setPrecision: function (precision) {
        this.precision = precision;
    },

    /*
     * @Override
     */
    toString: function () {
        return "thinkehr.f4.Validation";
    }
});

//-Models------------------------------------------------------------------------------------------------------------------------------------------------------

thinkehr.f4.Model = thinkehr.f4.Object._extendM({

    init: function (properties) {
        this._super(properties);

        this.parentModel = null;
        this.childModels = [];

        if (this.descEl === undefined) {
            this.descEl = null;
        }
    },

    getChildModels: function () {
        return this.childModels;
    },

    getChildModel: function (i) {
        return this.childModels[i];
    },

    getChildCount: function () {
        return this.childModels.length;
    },

    getChildModelsMatching: function (func) {
        var models = [];
        for (var i = 0; i < this.childModels.length; i++) {
            var model = this.childModels[i];
            if (func(model)) {
                models.push(model);
            }
        }

        return models;
    },

    addChildModel: function (model) {
        model.setParentModel(this);
        this.childModels.push(model);
    },

    insertChildModelAt: function (index, model) {
        model.setParentModel(this);
        this.childModels.splice(index, 0, model);
    },

    removeChildModel: function (model) {
        var index = this.childModels.indexOf(model);
        if (index >= 0) {
            this.childModels.splice(index, 1);
        }
    },

    removeFromParent: function () {
        if (this.getParentModel()) {
            this.getParentModel().removeChildModel(this);
        }
    },

    getParentModel: function () {
        return this.parentModel;
    },

    setParentModel: function (model) {
        this.parentModel = model;
    },

    getDescEl: function () {
        return this.descEl;
    },

    setDescEl: function (descEl) {
        this.descEl = descEl;
    },

    /*
     * @Override
     */
    toString: function () {
        return "thinkehr.f4.Model";
    }
});

thinkehr.f4.FormObjectModel = thinkehr.f4.Model._extendM({

    init: function (properties) {
        this._super(properties);

        if (this.formId === undefined) {
            this.formId = null;
        }
        if (this.name === undefined) {
            this.name = null;
        }
        if (this.viewConfig === undefined) {
            this.viewConfig = null;
        } else if (this.viewConfig instanceof thinkehr.f4.ViewConfig) {
            this.viewConfig.setModel(this);
        }
        if (this.valueNodeRef === undefined) {
            this.valueNodeRef = null;
        }
        if (this.valueNodeParentRef === undefined) {
            this.valueNodeParentRef = null;
        }
        if (this._childPathIdMap === undefined) {
            this._childPathIdMap = {};
        }
        if (this.__onModelRefreshed === undefined) {
            this.__onModelRefreshed = [];
        }
    },

    getFormId: function () {
        return this.formId;
    },

    setFormId: function (id) {
        this.formId = id;
    },

    getName: function () {
        return this.name;
    },

    setName: function (name) {
        this.name = name;
    },

    getViewConfig: function () {
        return this.viewConfig;
    },

    setViewConfig: function (viewConfig) {
        this.viewConfig = viewConfig;
        if (viewConfig) {
            viewConfig.setModel(this);
        }
    },

    getRmType: function () {
        return null;
    },

    getPath: function () {
        return this.formId;
    },

    isValueModel: function () {
        return false;
    },

    isAttachableToValueNode: function () {
        return true;
    },

    getValueNodeRef: function () {
        return this.valueNodeRef;
    },

    setValueNodeRef: function (valueNodeRef) {
        this.valueNodeRef = valueNodeRef;
    },

    getValueNodeParentRef: function () {
        return this.valueNodeParentRef;
    },

    setValueNodeParentRef: function (valueNodeParentRef) {
        this.valueNodeParentRef = valueNodeParentRef;
    },

    isContainer: function () {
        return false;
    },

    labelFor: function (language) {
        var lbl;
        if (this.getViewConfig() && this.getViewConfig().getLabel()) {

            var labelConfig = this.getViewConfig().getLabel();
            if (labelConfig.isCustom()) {
                if (labelConfig.isUseLocalizations()) {
                    lbl = labelConfig.getLocalization(language);
                }
                if (!lbl) {
                    lbl = labelConfig.getValue();
                }
            } else if (labelConfig.isUseLocalizations()) {
                lbl = labelConfig.getLocalization(language);
            }
        }

        if (!lbl) {
            lbl = this.getLocalizedName(language);
            if (!lbl) {
                lbl = this.getLocalizedName();
            }
        }

        return lbl;
    },

    getContainerChildModels: function () {
        return this.getChildModelsMatching(function (model) {
            return model.isContainer();
        });
    },

    /* Override */
    addChildModel: function (model) {
        this._super(model);
        this._increaseChildPathIdIndexFor(model);
    },

    /* Override */
    insertChildModelAt: function (index, model) {
        this._super(index, model);
        this._increaseChildPathIdIndexFor(model);
    },

    _increaseChildPathIdIndexFor: function (model) {
        var path = model.getPath() ? model.getPath() : "none";
        if (this._childPathIdMap[path] !== undefined) {
            this._childPathIdMap[path] += 1;
        } else {
            this._childPathIdMap[path] = 0;
        }

        var idIndex = this._childPathIdMap[path];
        model._childPathIdIndex = idIndex;

        return idIndex;
    },

    getUniqueId: function () {
        if (!this.childPathId) {
            if (!this.getPath()) {
                this.childPathId = null;
            } else {
                var index = this._childPathIdIndex === undefined ? 0 : this._childPathIdIndex;
                var modelPath = this.getPath();
                var p = modelPath + ":" + index;
                var parent = this.getParentModel();
                while (parent != null) {
                    if (!parent.getPath() || parent.getRmType() === thinkehr.f4.RmType.FORM_DEFINITION) {
                        parent = null;
                    } else {
                        index = parent._childPathIdIndex === undefined ? 0 : parent._childPathIdIndex;
                        var parentPath = parent.getPath();
                        if (p.indexOf(parentPath) == 0) {
                            var parentPart = p.substring(0, parentPath.length);
                            p = parentPart + ":" + index + p.substr(parentPath.length);
                        } else {
                            p = parentPath + ":" + index + "/" + p;
                        }
                        parent = parent.getParentModel();
                    }
                }

                this.childPathId = p;
            }
        }

        return this.childPathId;
    },

    getSanitizedUniqueId: function() {
        var uniqueId = this.getUniqueId();

        if (!uniqueId) {
            return null;
        }

        return uniqueId.replace(/[\/:-]/g, "_");
    },

    findAncestorWithPath: function (path) {
        var pm = this.getParentModel();
        while (pm) {
            if (pm.getPath() === path) {
                return pm;
            }

            pm = pm.getParentModel();
        }

        return null;
    },

    findChildWithPath: function (path, index) {
        if (this.getChildCount() > 0) {
            var ic = 0;
            for (var i = 0; i < this.getChildCount(); i++) {
                var childModel = this.getChildModel(i);
                if (childModel.getPath() === path) {
                    if (index === undefined) {
                        return childModel;
                    } else {
                        if (ic++ === index) {
                            return childModel;
                        }
                    }
                }
            }
        }

        return null;
    },

    findChildrenWithPath: function (path) {
        var children = [];
        for (var i = 0; i < this.getChildCount(); i++) {
            var childModel = this.getChildModel(i);
            if (childModel.getPath() === path) {
                children.push(childModel);
            }
        }

        return children;
    },

    findSuccessorWithPath: function (path) {
        for (var i = 0; i < this.getChildCount(); i++) {
            var childModel = this.getChildModel(i);
            if (childModel.getPath() === path) {
                return childModel;
            } else {
                var cm = childModel.findSuccessorWithPath(path);
                if (cm != null) {
                    return cm;
                }
            }
        }

        return null;
    },

    onModelRefreshed: function (f) {
        this.__onModelRefreshed.push(f);
    },

    _triggerModelRefreshed: function () {
        for (var i = 0; i < this.__onModelRefreshed.length; i++) {
            var f = this.__onModelRefreshed[i];
            f(this);
        }
    },

    /*
     * @Override
     */
    toString: function () {
        return "thinkehr.f4.FormObjectModel";
    }
});

thinkehr.f4.FormRootModel = thinkehr.f4.FormObjectModel._extendM({

    init: function (properties) {
        this._super(properties);
    },

    /*
     * @Override
     */
    getRmType: function () {
        return thinkehr.f4.RmType.FORM_DEFINITION;
    },

    /*
     * @Override
     */
    getUniqueId: function () {
        return null;
    },

    /*
     * @Override
     */
    toString: function () {
        return "thinkehr.f4.FormRootModel/" + this.getName() + "/" + this.getRmType().toString();
    }
});

thinkehr.f4.FormRepeatableElementModel = thinkehr.f4.FormObjectModel._extendM({
    init: function (properties) {
        this._super(properties);

        if (this.localizedName === undefined) {
            this.localizedName = null;
        }
        if (this.localizedNames === undefined) {
            this.localizedNames = {};
        }
        if (this.min === undefined) {
            this.min = null;
        }
        if (this.max === undefined) {
            this.max = null;
        }
        if (this.parent === undefined) {
            this.parent = null;
        }
    },

    getLocalizedName: function (locale) {
        if (locale) {
            var n = this.localizedNames[locale];
            return n ? n : null;
        }

        return this.localizedName;
    },

    setLocalizedName: function (localizedName) {
        this.localizedName = localizedName;
    },

    setLocalizedNames: function (localizedNames) {
        this.localizedNames = localizedNames;
    },

    getMin: function () {
        return this.min;
    },

    setMin: function (min) {
        this.min = min;
    },

    getMax: function () {
        return this.max;
    },

    setMax: function (max) {
        this.max = max;
    },

    isMulti: function () {
        return this.max && (this.max > 1 || this.max < 0);
    },

    /*
     * @Override
     */
    toString: function () {
        return "thinkehr.f4.FormRepeatableElementModel";
    }
});

thinkehr.f4.GenericFieldsetModel = thinkehr.f4.FormRepeatableElementModel._extendM({
    init: function (properties) {
        this._super(properties);
    },

    /*
     * @Override
     */
    getRmType: function () {
        return thinkehr.f4.RmType.GENERIC_FIELDSET;
    },

    /*
     * @Override
     */
    isContainer: function () {
        return true;
    },

    /*
     * @Override
     */
    isAttachableToValueNode: function () {
        return false;
    },

    /*
     * @Override
     */
    toString: function () {
        return "thinkehr.f4.GenericFieldsetModel/" + this.getName() + "/" + this.getRmType().toString();
    }
});

thinkehr.f4.RmContainerModel = thinkehr.f4.FormRepeatableElementModel._extendM({
    init: function (properties) {
        this._super(properties);

        if (this.rmType === undefined) {
            this.rmType = null;
        }
    },

    /*
     * @Override
     */
    getRmType: function () {
        return this.rmType;
    },

    setRmType: function (rmType) {
        this.rmType = rmType;
    },

    /*
     * @Override
     */
    isContainer: function () {
        return true;
    },

    /*
     * @Override
     */
    toString: function () {
        return "thinkehr.f4.RmContainerModel/" + this.getName() + "/" + this.getRmType();
    }
});

thinkehr.f4.NodeModel = thinkehr.f4.FormRepeatableElementModel._extendM({
    init: function (properties) {
        this._super(properties);

        if (this.nodeId === undefined) {
            this.nodeId = null;
        }

        if (this.aqlPath === undefined) {
            this.aqlPath = null;
        }

        if (this.inputs === undefined) {
            this.inputs = [];
        }

        if (this.defaultValue === undefined) {
            this.defaultValue = null;
        }

        if (this.value === undefined) {
            this.value = null;
        }
    },

    getNodeId: function () {
        return this.nodeId;
    },

    setNodeId: function (nodeId) {
        this.nodeId = nodeId;
    },

    getAqlPath: function () {
        return this.aqlPath;
    },

    setAqlPath: function (aqlPath) {
        this.aqlPath = aqlPath;
    },

    getInputs: function () {
        return this.inputs;
    },

    getInput: function (index) {
        return index >= 0 && index < this.inputs.length ? this.inputs[index] : null;
    },

    getInputFor: function (suffix) {
        for (var i = 0; i < this.inputs.length; i++) {
            var input = this.inputs[i];
            if (input.getSuffix() === suffix) {
                return input;
            }
        }

        return null;
    },

    setInputs: function (inputs) {
        this.inputs = inputs;
    },

    addInput: function (input) {
        this.inputs.push(input);
    },

    /*
     * Override
     */
    isValueModel: function () {
        return true;
    },

    getDefaultValue: function () {
        return this.defaultValue;
    },

    setDefaultValue: function (defaultValue) {
        this.defaultValue = defaultValue;
    },

    getValue: function (property) {
        if (property) {
            if (this.value) {
                var val = this.value[property];
                if (val === undefined) {
                    val = this.value["|" + property];
                }
                return val !== undefined ? val : null;
            } else {
                return null;
            }
        }

        return this.value;
    },

    setValue: function (value) {
        this.value = value;
    },

    clearValue: function () {
        this.value = null;
    },

    /*
     * @Override
     */
    toString: function () {
        return "thinkehr.f4.NodeModel";
    }
});

thinkehr.f4.QuantityFieldModel = thinkehr.f4.NodeModel._extendM({
    init: function (properties) {
        this._super(properties);
    },

    /*
     * @Override
     */
    getRmType: function () {
        return thinkehr.f4.RmType.DV_QUANTITY;
    },

    magnitudeValue: function (value) {
        var val = this.value;

        if (value !== undefined) {
            var actualValue = value && thinkehr.f4.util.isString(value) ? parseFloat(value) : value;
            if (actualValue === null || value === "") {
                this.clearMagnitudeValue();
                return null;
            } else {
                if (val === null) {
                    val = {};
                    this.getValueNodeParentRef().push(val);
                    this.setValueNodeRef(val);
                    this.setValue(val);
                }

                val["|magnitude"] = actualValue;
            }
        } else if (val == null) {
            if (this.getDefaultValue()) {
                var dv = this.getDefaultValue();
                if (dv.magnitude !== undefined) {

                    if (thinkehr.f4.util.isString(dv.unit) && dv.unit.length > 0) {
                        this.unitValue(dv.unit)
                    }

                    if (!thinkehr.f4.util.isString(dv.magnitude) || dv.magnitude.length > 0) {
                        return this.magnitudeValue(dv.magnitude)
                    }

                }
            }

            return null;
        }

        return val["|magnitude"];
    },

    unitValue: function (value) {
        var val = this.value;

        if (value !== undefined) {
            if (value === null || value === "") {
                this.clearUnitValue();
                return null;
            } else {
                if (val === null) {
                    val = {};
                    this.getValueNodeParentRef().push(val);
                    this.setValueNodeRef(val);
                    this.setValue(val);
                }

                val["|unit"] = value;
            }
        } else if (val == null) {
            if (this.getDefaultValue()) {
                var dv = this.getDefaultValue();
                if (!thinkehr.f4.util.isString(dv.magnitude) || dv.magnitude.length > 0) {
                    this.magnitudeValue(dv.magnitude)
                }

                if (thinkehr.f4.util.isString(dv.unit) && dv.unit.length > 0) {
                    return this.unitValue(dv.unit)
                }
            }

            return null;
        }

        return val["|unit"];
    },

    getPrecisionForUnit: function (unit) {
        var u = unit !== undefined ? unit : this.unitValue();
        if (u == null) {
            return null;
        }

        var isInt = thinkehr.f4.util.isInteger(unit);
        var l = this.getInputFor("unit").getList();
        for (var i = 0; i < l.length; i++) {
            var item = l[i];
            if ((isInt && u == i) || item.getValue() === u) {
                if (item.getValidation() && item.getValidation().getPrecision()) {
                    return item.getValidation().getPrecision();
                } else {
                    return null;
                }
            }
        }

        return null;
    },

    /*
     * @Override
     */
    clearValue: function () {
        var val = this.value;
        if (val) {
            val["|magnitude"] = null;
            val["|unit"] = null;
        }
    },

    clearUnitValue: function () {
        var val = this.value;
        if (val) {
            val["|unit"] = null;
        }
    },

    clearMagnitudeValue: function () {
        var val = this.value;
        if (val) {
            val["|magnitude"] = null;
        }
    },

    /*
     * @Override
     */
    toString: function () {
        return "thinkehr.f4.QuantityFieldModel/" + this.getName() + "/" + this.getRmType().toString();
    }
});

thinkehr.f4.CodeValueBasedFieldModel = thinkehr.f4.NodeModel._extendM({
    init: function (properties) {
        this._super(properties);
    },

    codeValue: function (value, language) {
        var val = this.value;

        if (value !== undefined) {
            if (value === null) {
                this.clearValue();
                return null;
            } else {
                if (val === null) {
                    val = {};
                    this.getValueNodeParentRef().push(val);
                    this.setValueNodeRef(val);
                    this.setValue(val);
                }

                val["|code"] = value;

                var input = this._getCodeInput();
                for (var i = 0; i < input.getList().length; i++) {
                    var item = input.getItem(i);
                    if (item.value === value) {
                        var labelValue = language !== undefined ? item.getLabel(language) : item.getLabel();
                        if (!labelValue) {
                            labelValue = item.getLabel();
                        }
                        val["|value"] = labelValue;
                        this._updateOtherFields(val, item);
                        break;
                    }
                }

                this._resetOtherFields(val);
            }
        } else if (val == null) {
            if (val == null && this.getDefaultValue()) {
                var dvCode = this.defaultValueCode();
                if (dvCode) {
                    return this.codeValue(dvCode, language);
                }
            }

            return null;
        }

        return val["|code"];
    },

    defaultValueCode: function () {
        var dv = this.getDefaultValue();
        if (thinkehr.f4.util.isString(dv.code) && dv.code.length > 0) {
            return dv.code;
        }

        return null;
    },

    _getCodeInput: function () {
        return this.getInputFor("code");
    },

    _updateOtherFields: function (val, item) {
        // For override
    },

    _resetOtherFields: function (val) {
        // For override
    },

    /*
     * @Override
     */
    clearValue: function () {
        var val = this.value;
        if (val) {
            val["|code"] = null;
            val["|value"] = null;
        }
    },

    /*
     * @Override
     */
    toString: function () {
        return "thinkehr.f4.CodeValueBasedFieldModel";
    }
});

thinkehr.f4.CodedTextFieldModel = thinkehr.f4.CodeValueBasedFieldModel._extendM({
    init: function (properties) {
        this._super(properties);
    },

    /*
     * @Override
     */
    _resetOtherFields: function (val) {
        val["|terminology"] = "local";
    },

    /*
     * @Override
     */
    clearValue: function () {
        this._super();
        if (this.value) {
            this.value["|terminology"] = null;
        }
    },

    /*
     * @Override
     */
    getRmType: function () {
        return thinkehr.f4.RmType.DV_CODED_TEXT;
    },

    /*
     * @Override
     */
    toString: function () {
        return "thinkehr.f4.CodedTextFieldModel/" + this.getName() + "/" + this.getRmType().toString();
    }
});

thinkehr.f4.OrdinalFieldModel = thinkehr.f4.CodeValueBasedFieldModel._extendM({
    init: function (properties) {
        this._super(properties);
    },

    /*
     * @Override
     */
    _getCodeInput: function () {
        return this.getInput(0);
    },

    /*
     * @Override
     */
    _updateOtherFields: function (val, item) {
        val["|ordinal"] = item["ordinal"];
    },

    /*
     * @Override
     */
    defaultValueCode: function () {
        var dv = this.getDefaultValue();
        if (thinkehr.f4.util.isString(dv.value) && dv.value.length > 0) {
            return dv.value;
        }

        return null;
    },

    /*
     * @Override
     */
    getInputFor: function (suffix) {
        return suffix == "code" ? this.getInput(0) : this._super(suffix);
    },

    /*
     * @Override
     */
    clearValue: function () {
        this._super();
        if (this.value) {
            this.value["|ordinal"] = null;
        }
    },

    /*
     * @Override
     */
    getRmType: function () {
        return thinkehr.f4.RmType.DV_ORDINAL;
    },

    /*
     * @Override
     */
    toString: function () {
        return "thinkehr.f4.OrdinalFieldModel/" + this.getName() + "/" + this.getRmType().toString();
    }
});

thinkehr.f4.TextFieldModel = thinkehr.f4.NodeModel._extendM({
    init: function (properties) {
        this._super(properties);
    },

    textValue: function (value) {
        if (value !== undefined) {

            this.value = value;
            var parentRef = this.getValueNodeParentRef();
            if (parentRef.length > 0) {
                if (value != null) {
                    parentRef[0] = this.value;
                } else {
                    parentRef.shift();
                }
            } else if (value !== null) {
                parentRef.push(this.value);
            }
        }
        else if (this.value == null && this.getDefaultValue()) {
            var dv = this.getDefaultValue();
            if (thinkehr.f4.util.isString(dv.value) && dv.value.length > 0) {
                return this.textValue(dv.value);
            }
        }

        return this.value;
    },

    /*
     * @Override
     */
    getRmType: function () {
        return thinkehr.f4.RmType.DV_TEXT;
    },

    /*
     * @Override
     */
    toString: function () {
        return "thinkehr.f4.TextFieldModel/" + this.getName() + "/" + this.getRmType().toString();
    }
});

thinkehr.f4.ProportionFieldModel = thinkehr.f4.NodeModel._extendM({
    init: function (properties) {
        this._super(properties);
    },

    numeratorValue: function (value) {
        var val = this.value;

        if (value !== undefined) {
            var actualValue = value && thinkehr.f4.util.isString(value) ? parseFloat(value) : value;
            if (actualValue === null || value === "") {
                this.clearNumeratorValue();
                return null;
            } else {
                if (val === null) {
                    val = {};
                    this.getValueNodeParentRef().push(val);
                    this.setValueNodeRef(val);
                    this.setValue(val);
                }

                val["|numerator"] = actualValue;
            }
        } else if (val == null) {
            if (this.getDefaultValue()) {
                var dv = this.getDefaultValue();
                if (dv.numerator !== undefined) {

                    if (thinkehr.f4.util.isString(dv.denominator) && dv.denominator.length > 0) {
                        this.denominatorValue(dv.denominator);
                    }

                    if (!thinkehr.f4.util.isString(dv.numerator) || dv.numerator.length > 0) {
                        return this.numeratorValue(dv.numerator);
                    }

                }
            }

            return null;
        }

        if (thinkehr.f4.util.isString(val["|numerator"])) {
            val["|numerator"] = parseFloat(val["|numerator"]);
        }

        return val["|numerator"];
    },

    denominatorValue: function (value) {
        var val = this.value;

        if (value !== undefined) {
            var actualValue = value && thinkehr.f4.util.isString(value) ? parseFloat(value) : value;
            if (actualValue === null || value === "") {
                this.clearDenominatorValue();
                return null;
            } else {
                if (val === null) {
                    val = {};
                    this.getValueNodeParentRef().push(val);
                    this.setValueNodeRef(val);
                    this.setValue(val);
                }

                val["|denominator"] = actualValue;
            }
        } else if (val == null) {
            if (this.getDefaultValue()) {
                var dv = this.getDefaultValue();
                if (dv.denominator !== undefined) {

                    if (thinkehr.f4.util.isString(dv.numerator) && dv.numerator.length > 0) {
                        this.numeratorValue(dv.numerator)
                    }

                    if (!thinkehr.f4.util.isString(dv.denominator) || dv.denominator.length > 0) {
                        return this.denominatorValue(dv.denominator)
                    }
                }
            }

            return null;
        }

        if (thinkehr.f4.util.isString(val["|denominator"])) {
            val["|denominator"] = parseFloat(val["|denominator"]);
        }

        return val["|denominator"];
    },

    /*
     * @Override
     */
    getRmType: function () {
        return thinkehr.f4.RmType.DV_PROPORTION;
    },

    clearDenominatorValue: function () {
        var val = this.value;
        if (val) {
            val["|denominator"] = null;
        }
    },

    clearNumeratorValue: function () {
        var val = this.value;
        if (val) {
            val["|numerator"] = null;
        }
    },

    /*
     * @Override
     */
    toString: function () {
        return "thinkehr.f4.ProportionFieldModel/" + this.getName() + "/" + this.getRmType().toString();
    }
});

thinkehr.f4.BooleanFieldModel = thinkehr.f4.NodeModel._extendM({
    init: function (properties) {
        this._super(properties);
        this._clearedValue = false;
    },

    booleanValue: function (value) {

        if (value !== undefined) {

            this.value = value;
            var parentRef = this.getValueNodeParentRef();
            if (parentRef.length > 0) {
                if (value != null) {
                    parentRef[0] = this.value;
                    this._clearedValue = false;
                } else {
                    parentRef.shift();
                    this._clearedValue = true;
                }
            } else if (value !== null) {
                parentRef.push(this.value);
                this._clearedValue = false;
            }
        }
        else if ((this.value == null || this.value === "") && !this._clearedValue) {
            var val = this.calculateDefaultValue();
            if (val === null) {
                if (!this.isThreeState()) {
                    return this.booleanValue(false);
                }
            } else {
                return this.booleanValue(val);
            }
        }

        return this.value;
    },

    calculateDefaultValue: function () {
        var val;
        if (this.getDefaultValue()) {
            var dv = this.getDefaultValue();
            var isStr = thinkehr.f4.util.isString(dv.value);
            if ((dv.value !== undefined && !isStr) || (isStr && dv.value.length > 0)) {
                val = dv.value;
                if (isStr) {
                    val = val === "true";
                }
            } else if (!this.isThreeState()) {
                val = false;
            } else {
                val = null;
            }
        } else if (this.isThreeState()) {
            val = null;
        } else {
            val = false;
        }

        if (this.allowedValues().indexOf(val) < 0) {
            val = !val;
        }

        return val;
    },

    /*
     * @Override
     */
    getRmType: function () {
        return thinkehr.f4.RmType.DV_BOOLEAN;
    },

    clearBooleanValue: function () {
        var parentRef = this.getValueNodeParentRef();
        if (parentRef && parentRef.length > 0) {
            if (this.isThreeState()) {
                parentRef.shift();
                this.value = null;
            } else {
                this.value = false;
                parentRef[0] = this.value;
            }
        }

        this._clearedValue = true;
    },

    isThreeState: function() {
        return this.getViewConfig().getField() && this.getViewConfig().getField().threeState;
    },

    allowedValues: function () {
        if (!this._allowedValues) {
            this._allowedValues = [];

            var input = this.getInput(0);
            if (input.hasItems()) {
                for (var i = 0; i < input.getList().length; i++) {
                    var item = input.getItem(i);
                    var val = item.value;
                    if (val !== undefined) {
                        if (thinkehr.f4.util.isString(val)) {
                            if (val === "true") {
                                this._allowedValues.push(true);
                            } else if (val === "false") {
                                this._allowedValues.push(false);
                            }
                        } else {
                            this._allowedValues.push()
                        }
                    }
                }
            } else {
                this._allowedValues.push(true, false);
            }

            if (this.isThreeState()) {
                this._allowedValues.push(null);
            }
        }

        return this._allowedValues;
    },

    /*
     * @Override
     */
    toString: function () {
        return "thinkehr.f4.BooleanFieldModel/" + this.getName() + "/" + this.getRmType().toString();
    }
});

thinkehr.f4.DirectValueModel = thinkehr.f4.NodeModel._extendM({
    init: function (properties, valueFuncName) {
        this._super(properties);
        this._clearedValue = false;
        this._valueFuncName = valueFuncName;
    },

    valueGetterSetter: function (value) {
        if (value !== undefined) {

            this.value = value;
            var parentRef = this.getValueNodeParentRef();
            if (parentRef.length > 0) {
                if (value != null) {
                    parentRef[0] = this.value;
                    this._clearedValue = false;
                } else {
                    parentRef.shift();
                    this._clearedValue = true;
                }
            } else if (value !== null) {
                parentRef.push(this.value);
                this._clearedValue = false;
            }
        }
        else if (this.value == null && this.getDefaultValue() && !this._clearedValue) {
            var dv = this.getDefaultValue();
            if (thinkehr.f4.util.isString(dv.value) && dv.value.length > 0) {
                return this.valueGetterSetter(dv.value);
            }
        }

        return this.value;
    },


    /*
     * @Override
     */
    clearValue: function () {
        var parentRef = this.getValueNodeParentRef();
        if (parentRef && parentRef.length > 0) {
            parentRef.shift();
        }

        this._clearedValue = true;
        this.value = null;
    },

    /*
     * @Override
     */
    toString: function () {
        return "thinkehr.f4.DirectValueModel/" + this._valueFuncName;
    }
});

thinkehr.f4.DateFieldModel = thinkehr.f4.DirectValueModel._extendM({
    init: function (properties) {
        this._super(properties, "dateValue");
    },

    dateValue: function (value) {
        return this.valueGetterSetter(value);
    },

    /*
     * @Override
     */
    getRmType: function () {
        return thinkehr.f4.RmType.DV_DATE;
    },

    clearDateValue: function () {
        this.clearValue();
    },

    /*
     * @Override
     */
    toString: function () {
        return "thinkehr.f4.DateFieldModel/" + this.getName() + "/" + this.getRmType().toString();
    }
});

thinkehr.f4.TimeFieldModel = thinkehr.f4.DirectValueModel._extendM({
    init: function (properties) {
        this._super(properties, "timeValue");
    },

    timeValue: function (value) {
        return this.valueGetterSetter(value);
    },

    /*
     * @Override
     */
    getRmType: function () {
        return thinkehr.f4.RmType.DV_TIME;
    },

    clearTimeValue: function () {
        this.clearValue();
    },

    /*
     * @Override
     */
    toString: function () {
        return "thinkehr.f4.TimeFieldModel/" + this.getName() + "/" + this.getRmType().toString();
    }
});

thinkehr.f4.DateTimeFieldModel = thinkehr.f4.DirectValueModel._extendM({
    init: function (properties) {
        this._super(properties, "dateTimeValue");
    },

    dateTimeValue: function (value) {
        return this.valueGetterSetter(value);
    },

    /*
     * @Override
     */
    getRmType: function () {
        return thinkehr.f4.RmType.DV_DATE_TIME;
    },

    clearDateTimeValue: function () {
        this.clearValue();
    },

    /*
     * @Override
     */
    toString: function () {
        return "thinkehr.f4.DateTimeFieldModel/" + this.getName() + "/" + this.getRmType().toString();
    }
});

// A map of rmType string to class mappings

thinkehr.f4._rmTypeToClassMap = {
    "FORM_DEFINITION": thinkehr.f4.FormRootModel,
    "GENERIC_FIELDSET": thinkehr.f4.GenericFieldsetModel,
    "DV_QUANTITY": thinkehr.f4.QuantityFieldModel,
    "DV_CODED_TEXT": thinkehr.f4.CodedTextFieldModel,
    "DV_TEXT": thinkehr.f4.TextFieldModel,
    "DV_PROPORTION": thinkehr.f4.ProportionFieldModel,
    "DV_BOOLEAN": thinkehr.f4.BooleanFieldModel,
    "DV_DATE": thinkehr.f4.DateFieldModel,
    "DV_TIME": thinkehr.f4.TimeFieldModel,
    "DV_DATE_TIME": thinkehr.f4.DateTimeFieldModel,
    "DV_ORDINAL": thinkehr.f4.OrdinalFieldModel
};

