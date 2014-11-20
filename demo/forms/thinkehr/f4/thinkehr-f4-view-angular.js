// Namespace definitions
if (!thinkehr) {
    var thinkehr = {};
}
if (!thinkehr.f4) {
    thinkehr.f4 = {};
}
if (!thinkehr.f4.util) {
    thinkehr.f4.util = {};
}

if (!thinkehr.f4.ng) {
    thinkehr.f4.ng = {};
    thinkehr.f4.ng.CTRL_NAME = thinkehr.f4.PREFIX + "FormCtrl";
}

(function namespace() {

    function _recursionHelperService() {
        return angular.module('EhrRecursionHelper', []).factory('EhrRecursionHelper', ['$compile', function ($compile) {
            return {
                /*
                 * Manually compiles the element, fixing the recursion loop.
                 * @param element
                 * @param [link] A post-link function, or an object with function(s) registered via pre and post properties.
                 * @returns An object containing the linking functions.
                 */
                compile: function (element, link) {
                    // Normalize the link parameter
                    if (angular.isFunction(link)) {
                        link = { post: link };
                    }

                    // Break the recursion loop by removing the contents
                    var contents = element.contents().remove();
                    var compiledContents;
                    return {
                        pre: (link && link.pre) ? link.pre : null,
                        /*
                         * Compiles and re-adds the contents
                         */
                        post: function (scope, element) {
                            // Compile the contents
                            if (!compiledContents) {
                                compiledContents = $compile(contents);
                            }
                            // Re-add the compiled contents to the element
                            compiledContents(scope, function (clone) {
                                element.append(clone);
                            });

                            // Call the post-linking function, if any
                            if (link && link.post) {
                                link.post.apply(null, arguments);
                            }
                        }
                    };
                }
            };
        }]);
    }

    function _createContext(source, dest) {
        var ehrContext = angular.extend(dest, source);

        var locale;
        if (ehrContext.language) {
            locale = ehrContext.language;
            if (ehrContext.territory) {
                locale += "-" + ehrContext.territory.toUpperCase();
            }
        } else {
            locale = null;
        }

        ehrContext.locale = locale;

        return ehrContext;
    }


    function createFormModule(formName, formVersion, formModel, context, fixedName) {
        _recursionHelperService();

        // Define this empty module if the template module has not been produced and included by grunt
        if (!thinkehr.f4.ng._ehrTemplatesDefined) {
            console.warn("ehr-forms4-templates, defining empty module", thinkehr.f4.ng._ehrTemplatesDefined);
            angular.module("ehr-forms4-templates", []);
            thinkehr.f4.ng._ehrTemplatesDefined = true;
        }

        var moduleName = fixedName ? fixedName : thinkehr.f4.PREFIX + formName + ":" + formVersion;
        var module = angular.module(moduleName, ["EhrRecursionHelper", "ehr-forms4-templates" ]);

        module.factory("EhrContext", [function () {
            return _createContext(context, {});
        }]);

        module.factory("EhrSaveHelper", ["EhrContext", function (EhrContext) {
            return {
                prepareValueModel: function (valueModel) {
                    var clonedModel = angular.copy(valueModel);

                    this.createSaveContext(EhrContext, clonedModel);

                    return clonedModel;
                },

                createSaveContext: function (context, valueModel) {
                    if (!valueModel.ctx) {
                        valueModel.ctx = {};
                    }

                    var ctx = context ? context : EhrContext;

                    if (ctx.language) {
                        valueModel.ctx.language = ctx.language;
                    }

                    if (ctx.territory) {
                        valueModel.ctx.territory = ctx.territory;
                    }

                    return valueModel.ctx;
                }
            }
        }]);

        module.factory("EhrLayoutHelper", ["lowercaseFilter", function (lowercaseFilter) {
            return {
                fieldSizeClass: function (model) {

                    if (model.getViewConfig().getSize() && model.getViewConfig().getSize().getField()) {
                        var fs = model.getViewConfig().getSize().getField();
                        if (fs != thinkehr.f4.FieldSize.INHERIT) {
                            return "field-size-" + lowercaseFilter(fs.toString());
                        }
                    }

                    return null;
                },

                fieldHorizontalAlignClass: function (model) {
                    if (model.getViewConfig().getLayout() && model.getViewConfig().getLayout().getFieldHorizontalAlignment()) {
                        var fha = model.getViewConfig().getLayout().getFieldHorizontalAlignment();
                        if (fha != thinkehr.f4.FieldHorizontalAlignment.INHERIT) {
                            return "field-horizontal-align-" + lowercaseFilter(fha.toString());
                        }
                    }
                },

                fieldVerticalAlignClass: function (model) {
                    if (model.getViewConfig().getLayout() && model.getViewConfig().getLayout().getFieldVerticalAlignment()) {
                        var fva = model.getViewConfig().getLayout().getFieldVerticalAlignment();
                        if (fva != thinkehr.f4.FieldVerticalAlignment.INHERIT) {
                            return "field-vertical-align-" + lowercaseFilter(fva.toString());
                        }
                    }
                },

                labelHorizontalAlignClass: function (model) {
                    if (model.getViewConfig().getLayout() && model.getViewConfig().getLayout().getLabelHorizontalAlignment()) {
                        var lha = model.getViewConfig().getLayout().getLabelHorizontalAlignment();
                        if (lha != thinkehr.f4.LabelHorizontalAlignment.INHERIT) {
                            return "label-horizontal-align-" + lowercaseFilter(lha.toString());
                        }
                    }
                },

                labelVerticalAlignClass: function (model) {
                    if (model.getViewConfig().getLayout() && model.getViewConfig().getLayout().getLabelVerticalAlignment()) {
                        var lva = model.getViewConfig().getLayout().getLabelVerticalAlignment();
                        if (lva != thinkehr.f4.LabelVerticalAlignment.INHERIT) {
                            return "label-vertical-align-" + lowercaseFilter(lva.toString());
                        }
                    }
                },

                computeFieldClass: function (model) {
                    var c = [];
                    // Will add others
                    var fsc = this.fieldSizeClass(model);
                    if (fsc) {
                        c.push(fsc);
                    }

                    return c;
                },

                computeEhrLineClass: function (model) {
                    var c = [];

                    var fha = this.fieldHorizontalAlignClass(model);
                    if (fha) {
                        c.push(fha);
                    }

                    var fva = this.fieldVerticalAlignClass(model);
                    if (fva) {
                        c.push(fva);
                    }

                    var lha = this.labelHorizontalAlignClass(model);
                    if (lha === "label-horizontal-align-top") {
                        c.push("line-top-to-bottom");
                    }

                    return c;
                },

                computeEhrLabelContentClass: function (model) {
                    var c = [];
                    // Will add others
                    var lha = this.labelHorizontalAlignClass(model);
                    if (lha) {
                        c.push(lha);
                    }

                    var lva = this.labelVerticalAlignClass(model);
                    if (lva) {
                        c.push(lva);
                    }

                    return c;
                },

                distributeColumns: function (columns, elements) {
                    var totalCols = elements < columns ? elements : columns;
                    var cols = [];

                    for (var i = 0; i < totalCols; i++) {
                        cols.push([]);
                    }

                    for (i = 0; i < elements; i++) {
                        var colIndex = i % totalCols;
                        cols[colIndex].push(i);
                    }

                    return cols;

                },

                columnWidthPercentage: function (viewConfig, columns) {
                    var vc = viewConfig;
                    if (vc.getSize() && vc.getSize().getField() === thinkehr.f4.FieldSize.LARGE) {
                        return Math.floor((100 / columns) - 1).toString() + "%";
                    }

                    return "auto";
                }
            }
        }]);

        module.directive("ehrForm", ["$log", "EhrContext", "EhrLayoutHelper", function ($log, EhrContext, EhrLayoutHelper) {
            return {
                restrict: "EA",
                templateUrl: "thinkehr/f4/templates/ehr-form.html",
                scope: {
                    model: "=",
                    ehrContext: "=",
                    formId: "@"
                },
                controller: function ($scope) {
                    if (!$scope.model) {
                        $scope.model = formModel;
                    }
                    $scope.$log = $log;
                    $scope.thinkehr = thinkehr;
                    $scope.console = console;
                    $scope.EhrContext = $scope.ehrContext ? _createContext($scope.ehrContext, EhrContext) : EhrContext;
                    $scope.EhrLayoutHelper = EhrLayoutHelper;
                }

            };
        }]);

        module.directive("ehrContainer", ["EhrRecursionHelper", function (EhrRecursionHelper) {
            return {
                restrict: "EA",
                templateUrl: "thinkehr/f4/templates/ehr-container.html",
                scope: true,
                controller: function ($scope) {
                    $scope.suppressLabel = false;
                    $scope._showControls = false;

                    $scope.supportsMulti = function () {
                        return $scope.model.isContainer() && $scope.model.isMulti() && $scope.model.isAttachableToValueNode();
                    };

                    $scope.duplicateContainer = function () {
                        thinkehr.f4.duplicateModel($scope.model, $scope.model);
                    };

                    $scope.removeContainer = function () {
                        thinkehr.f4.destroyModel($scope.model);
                    };

                    $scope.showControls = function (showControls) {
                        if (showControls !== undefined) {
                            $scope._showControls = showControls;
                        }

                        return $scope._showControls;
                    };

                    $scope.duplicationEnabled = function () {
                        if ($scope.supportsMulti()) {
                            var model = $scope.model;
                            var max = model.getViewConfig().getMax();
                            if (max < 0) {
                                return true;
                            }
                            var parent = model.getParentModel();
                            var count = parent ? parent.findChildrenWithPath(model.getPath()).length : 1;

                            return count < max;
                        }

                        return false;
                    };

                    $scope.removalEnabled = function () {
                        if ($scope.supportsMulti()) {
                            var model = $scope.model;
                            var min = model.getViewConfig().getMin();
                            if (min < 1) {
                                min = 1;
                            }
                            var parent = model.getParentModel();
                            var count = parent ? parent.findChildrenWithPath(model.getPath()).length : 1;

                            return count > min;
                        }

                        return false;
                    };

                },
                compile: function (element) {
                    return EhrRecursionHelper.compile(element, function (scope, iElement, iAttrs, controller, transcludeFn) {
                    });
                }
            };
        }]);

        module.directive("ehrRecursiveElement", function () {
            return {
                restrict: "EA",
                templateUrl: "thinkehr/f4/templates/ehr-recursive-element.html"
            };
        });

        module.directive("ehrUnknown", function () {
            return {
                restrict: "EA",
                templateUrl: "thinkehr/f4/templates/ehr-unknown.html",
                controller: function ($scope) {
                }
            };
        });

        module.directive("ehrLabel", function () {
            return {
                restrict: "E",
                templateUrl: "thinkehr/f4/templates/ehr-label.html",
                transclude: true,
                scope: true,
                controller: function ($scope, $element, $attrs) {

                }
            };
        });

        var _ehrCodedTextController = function ($scope, EhrContext, EhrLayoutHelper) {
            $scope.suppressLabel = false;

            $scope.codeValue = function (value) {
                return $scope.model.codeValue(value, EhrContext.language);
            };

            $scope.presentation = function () {
                var vc = $scope.model.getViewConfig();
                if (vc.getField() && vc.getField().getPresentation()) {
                    return vc.getField().getPresentation();
                }

                return thinkehr.f4.FieldPresentation.COMBOBOX;
            };

            $scope.list = function () {
                return $scope.model.getInputFor("code").getList();
            };

            $scope.codedTextName = function () {
                return "coded_text_" + $scope.model.getSanitizedUniqueId();
            };

            $scope.computeLineClass = function () {
                if (!$scope._ctLineClass) {
                    $scope._ctLineClass = EhrLayoutHelper.computeEhrLineClass($scope.model);
                    if ($scope.model.getRmType() === thinkehr.f4.RmType.DV_CODED_TEXT) {
                        $scope._ctLineClass.push("ehr-coded-text");
                    } else if ($scope.model.getRmType() === thinkehr.f4.RmType.DV_ORDINAL) {
                        $scope._ctLineClass.push("ehr-ordinal");
                    }
                }

                return $scope._ctLineClass;
            }
        };

        module.directive("ehrCodedText", ["EhrContext", "EhrLayoutHelper", function (EhrContext, EhrLayoutHelper) {
            return {
                restrict: "EA",
                templateUrl: "thinkehr/f4/templates/ehr-coded-text.html",
                scope: true,
                controller: function ($scope) {
                    _ehrCodedTextController($scope, EhrContext, EhrLayoutHelper);
                }
            };
        }]);

        module.directive("ehrOrdinal", ["EhrContext", "EhrLayoutHelper", function (EhrContext, EhrLayoutHelper) {
            return {
                restrict: "EA",
                templateUrl: "thinkehr/f4/templates/ehr-coded-text.html",
                scope: true,
                controller: function ($scope) {
                    _ehrCodedTextController($scope, EhrContext, EhrLayoutHelper);

                    $scope.codedTextName = function () {
                        return "ordinal_" + $scope.model.getSanitizedUniqueId();
                    };

                    $scope.ordinalName = function () {
                        return $scope.codedTextName();
                    };
                }
            };
        }]);

        module.directive("ehrCodedTextCombo", ["EhrContext", function (EhrContext) {
            return {
                restrict: "EA",
                templateUrl: "thinkehr/f4/templates/ehr-coded-text-combo.html",
                scope: true,
                controller: function ($scope) {
                    $scope.suppressLabel = false;
                    $scope.codeValue = function (value) {
                        var codeValue = $scope.model.codeValue(value, EhrContext.language);
                        //console.log("Value: " + codeValue);
                        return codeValue === undefined || codeValue === null ? "" : codeValue;
                    };
                }
            };
        }]);

        module.directive("ehrCodedTextRadio", ["EhrContext", "EhrLayoutHelper", function (EhrContext, EhrLayoutHelper) {
            return {
                restrict: "EA",
                templateUrl: "thinkehr/f4/templates/ehr-coded-text-radio.html",
                scope: true,
                controller: function ($scope) {
                    $scope.suppressLabel = false;

                    $scope.codeValue = function (value) {
                        return $scope.model.codeValue(value, EhrContext.language);
                    };

                    // This is an immediately executed function that assigns to an integer property because the distributeColumns() function returns a
                    // different instance of an array every time, which CAN cause a $digest cycle loop
                    $scope.columns = function () {
                        var model = $scope.model;
                        var cols = 1;
                        var vc = model.getViewConfig();
                        if (vc.getField()) {
                            if (vc.getField().getPresentation() === thinkehr.f4.FieldPresentation.RADIOS) {
                                if (vc.getField().getColumns()) {
                                    cols = vc.getField().getColumns();
                                }
                            }
                        }

                        if (cols < 1) {
                            cols = 1;
                        }

                        var elements = model.getInputFor("code").getList().length;
                        return EhrLayoutHelper.distributeColumns(cols, elements);
                    }();

                    $scope.columnWidthPercentage = function () {

                        return EhrLayoutHelper.columnWidthPercentage($scope.model.getViewConfig(), $scope.columns.length);
                    };

                    $scope.elementStyle = function () {
                        var colWidthPer = $scope.columnWidthPercentage();
                        if (colWidthPer != "auto") {
                            return {
                                width: colWidthPer
                            };
                        }
                        return {};
                    };
                }
            };
        }]);

        module.directive("ehrQuantity", ["$timeout", "EhrContext", function ($timeout, EhrContext) {
            return {
                restrict: "EA",
                templateUrl: "thinkehr/f4/templates/ehr-quantity.html",
                scope: true,
                controller: function ($scope) {

                    $scope.suppressLabel = false;

                    $scope.magnitudeValue = function (value) {
                        return $scope.model.magnitudeValue(value);
                    };

                    $scope.magnitude = $scope.magnitudeValue();

                    $scope.$watch('magnitude', function (magnitude) {
                        $scope.magnitudeValue(magnitude);
                    });

                    $scope.magnitudeName = function () {
                        return "magnitude_quantity_" + $scope.model.getSanitizedUniqueId();
                    };

                    $scope.unitName = function () {
                        return "unit_quantity_" + $scope.model.getSanitizedUniqueId();
                    };

                    $scope.list = function () {
                        return $scope.model.getInputFor("unit").getList();
                    };

                    $scope.model.onModelRefreshed(function (model) {

                        $scope.magnitude = $scope.magnitudeValue();

                    });

                    $scope.precision = function () {
                        var precision = $scope.model.getPrecisionForUnit(0);
                        if (precision == null) {
                            return 2;
                        }

                        return precision.max;
                    }();

                    $scope.format = function () {
                        return "n" + $scope.precision;
                    }();

                    $scope.step = function () {
                        return 1 / Math.pow(10, $scope.precision);

                    }();

                    $scope.magnitudeOptions = {
                        culture: EhrContext.locale,
                        decimals: $scope.precision,
                        format: $scope.format,
                        step: $scope.step
                    };


                    // Disable this once option rebinding works properly on input field via k-options and k-rebind. See
                    // ehr-quantity.html input field
                    $scope.$watch("precision", function (newVal, oldVal) {
                        // Have to this in jQuery since simply binding it via k-decimals and k-format does not auto-update
                        if (newVal !== oldVal) {
                            var input = $scope[$scope.magnitudeName()];


                            if (input) {
                                input.options.decimals = $scope.precision;
                                input.options.format = $scope.format;
                                input.options.culture = EhrContext.locale;
                                input.step($scope.step);

                                // Switch focuses so that the UI gets updated. Will run on next $digest
                                $timeout(function () {
                                    var input = $scope[$scope.magnitudeName()];
                                    if (input) {
                                        input.focus();
                                    }

                                    var select = $scope[$scope.unitName()];
                                    if (select) {
                                        select.focus();
                                    } else {
                                        select = $("ehr-form form ehr-quantity select[name='" + $scope.unitName() + "'].ehr-combo");
                                        if (select.length > 0) {
                                            var s = $(select).data("kendoComboBox");
                                            s.focus();
                                        }
                                    }
                                });
                            }
                        }
                    });


                    $scope.unitValue = function (value) {
                        var val = $scope.model.unitValue(value);

                        if (value !== undefined) {
                            var u = val != null ? val : 0;
                            var precision = $scope.model.getPrecisionForUnit(u);
                            if (precision == null) {
                                $scope.precision = 2;
                            } else {
                                $scope.precision = precision.max;
                            }

                            $scope.format = "n" + $scope.precision;
                            $scope.step = 1 / Math.pow(10, $scope.precision);

                            $scope.magnitudeOptions.decimals = $scope.precision;
                            $scope.magnitudeOptions.format = $scope.format;
                            $scope.magnitudeOptions.step = $scope.step;
                        }

                        return val === undefined || val === null ? "" : val;
                    };

                    $scope.presentation = function () {
                        var vc = $scope.model.getViewConfig();
                        if (vc.getField() && vc.getField().getPresentation()) {
                            return vc.getField().getPresentation();
                        }

                        return thinkehr.f4.FieldPresentation.COMBOBOX;
                    };
                }
            };
        }]);

        module.directive("ehrText", ["EhrContext", function (EhrContext) {
            return {
                restrict: "EA",
                templateUrl: "thinkehr/f4/templates/ehr-text.html",
                scope: true,
                controller: function ($scope) {
                    $scope.suppressLabel = false;
                    $scope.textValue = function (value) {
                        return $scope.model.textValue(value);
                    };
                }
            };
        }]);

        module.directive("ehrTextField", ["EhrContext", function (EhrContext) {
            return {
                restrict: "EA",
                templateUrl: "thinkehr/f4/templates/ehr-text-field.html",
                scope: true,
                controller: function ($scope) {
                    $scope.textValue = function (value) {
                        return $scope.model.textValue(value);
                    };
                }
            };
        }]);

        module.directive("ehrTextArea", ["EhrContext", function (EhrContext) {
            return {
                restrict: "EA",
                templateUrl: "thinkehr/f4/templates/ehr-text-area.html",
                scope: true,
                controller: function ($scope) {
                    $scope.rowsDefault = 4;

                    $scope.textValue = function (value) {
                        return $scope.model.textValue(value);
                    };

                    $scope.rows = function () {
                        if ($scope.model.getViewConfig().getField()) {
                            var field = $scope.model.getViewConfig().getField();

                            return field.getLines() ? field.getLines() : $scope.rowsDefault;
                        }

                        return $scope.rowsDefault;
                    };
                }
            };
        }]);

        module.directive("ehrProportion", ["EhrContext", "EhrLayoutHelper", function (EhrContext, EhrLayoutHelper) {
            return {
                restrict: "EA",
                templateUrl: "thinkehr/f4/templates/ehr-proportion.html",
                scope: true,
                controller: function ($scope) {
                    $scope.suppressLabel = false;

                    $scope.numeratorValue = function (value) {
                        return $scope.model.numeratorValue(value);
                    };

                    $scope.numerator = $scope.numeratorValue();

                    $scope.$watch('numerator', function (numerator) {
                        $scope.numeratorValue(numerator);
                    });

                    $scope.denominatorValue = function (value) {
                        return $scope.model.denominatorValue(value);
                    };

                    $scope.denominator = $scope.denominatorValue();

                    $scope.$watch('denominator', function (denominator) {
                        $scope.denominatorValue(denominator);
                    });

                    $scope._precision = function (input) {

                        if (input.getType() === thinkehr.f4.InputType.DECIMAL) {
                            if (input.getValidation()) {
                                var p = input.getValidation().precision;
                                return p ? p.max : 3;
                            } else {
                                return 3;
                            }
                        } else {
                            return 0;
                        }
                    };

                    $scope.numeratorPrecision = $scope._precision($scope.model.getInputFor("numerator"));
                    $scope.numeratorFormat = "n" + $scope.numeratorPrecision;
                    $scope.numeratorStep = 1 / Math.pow(10, $scope.numeratorPrecision);

                    $scope.denominatorPrecision = $scope._precision($scope.model.getInputFor("denominator"));
                    $scope.denominatorFormat = "n" + $scope.denominatorPrecision;
                    $scope.denominatorStep = 1 / Math.pow(10, $scope.denominatorPrecision);


                    $scope.numeratorName = function () {
                        return "numerator_proportion_" + $scope.model.getSanitizedUniqueId();
                    };

                    $scope.denominatorName = function () {
                        return "denominator_proportion_" + $scope.model.getSanitizedUniqueId();
                    };

                    $scope.numeratorOptions = {
                        culture: EhrContext.locale,
                        decimals: $scope.numeratorPrecision,
                        format: $scope.numeratorFormat,
                        step: $scope.numeratorStep
                    };

                    $scope.denominatorOptions = {
                        culture: EhrContext.locale,
                        decimals: $scope.denominatorPrecision,
                        format: $scope.denominatorFormat,
                        step: $scope.denominatorStep
                    };

                    $scope.percentage = function () {
                        var denominatorInput = $scope.model.getInputFor("denominator");
                        if (denominatorInput.getValidation()) {
                            var v = denominatorInput.getValidation();
                            return (v.range && v.range.min == 100.0 && v.range.max == 100.0);
                        }

                        return false;
                    };

                    $scope.fixedDenominator = function () {
                        var denominatorInput = $scope.model.getInputFor("denominator");
                        if (denominatorInput.getValidation()) {
                            var v = denominatorInput.getValidation();
                            return (v.range && v.range.min !== undefined && v.range.min == v.range.max);
                        }

                        return false;
                    };

                    $scope.model.onModelRefreshed(function () {
                        $scope.numerator = $scope.numeratorValue();
                        $scope.denominator = $scope.denominatorValue();
                    });

                    $scope.computeFieldClass = function () {
                        if (!$scope.fieldClass) {
                            var cl = EhrLayoutHelper.computeFieldClass($scope.model);
                            if ($scope.percentage()) {
                                cl.push("ehr-percentage");
                                cl.push("ehr-fixed-denominator")
                            } else if ($scope.fixedDenominator()) {
                                cl.push("ehr-fixed-denominator");
                            }

                            $scope.fieldClass = cl;
                        }

                        return $scope.fieldClass;
                    };
                }
            };
        }]);

        module.directive("ehrBoolean", ["EhrContext", "EhrLayoutHelper", function (EhrContext, EhrLayoutHelper) {
            return {
                restrict: "EA",
                templateUrl: "thinkehr/f4/templates/ehr-boolean.html",
                scope: true,
                controller: function ($scope) {
                    $scope.suppressLabel = false;

                    $scope.booleanName = function () {
                        return "boolean_" + $scope.model.getSanitizedUniqueId();
                    };

                    $scope.booleanValue = function (value) {
                        return $scope.model.booleanValue(value);
                    };

                    $scope.computeFieldClass = function () {
                        if (!$scope.fieldClass) {
                            var cl = EhrLayoutHelper.computeFieldClass($scope.model);
                            if ($scope.model.isThreeState()) {
                                cl.push("ehr-boolean-three-state");
                            } else {
                                cl.push("ehr-checkbox-two-state");
                            }

                            $scope.fieldClass = cl;
                        }

                        return $scope.fieldClass;
                    };

                    $scope.twoStateReadOnly = function () {
                        return $scope.model.getViewConfig().isReadOnly() || $scope.model.allowedValues().length < 2;
                    };

                    $scope.trueAllowed = function () {
                        return $scope.model.allowedValues().indexOf(true) >= 0;
                    };

                    $scope.falseAllowed = function () {
                        return $scope.model.allowedValues().indexOf(false) >= 0;
                    };

                    $scope.nullAllowed = function () {
                        return $scope.model.allowedValues().indexOf(null) >= 0;
                    };

                    $scope.trueReadOnly = function () {
                        return $scope.model.getViewConfig().isReadOnly() || !$scope.trueAllowed();
                    };

                    $scope.falseReadOnly = function () {
                        return $scope.model.getViewConfig().isReadOnly() || !$scope.falseAllowed();
                    };

                    $scope.nullReadOnly = function () {
                        return $scope.model.getViewConfig().isReadOnly() || !$scope.nullAllowed();
                    };

                    $scope.columnWidthPercentage = function () {

                        return EhrLayoutHelper.columnWidthPercentage($scope.model.getViewConfig(), 3);
                    };

                    $scope.columnStyle = function () {
                        if (!$scope._colStyle) {
                            var colWidthPer = $scope.columnWidthPercentage();
                            if (colWidthPer != "auto") {
                                $scope._colStyle = {
                                    width: colWidthPer
                                };
                            }
                            else {
                                $scope._colStyle = {};
                            }
                        }

                        return $scope._colStyle;
                    };
                }
            };
        }]);

        module.directive("ehrDate", ["dateFilter", "EhrContext", function (dateFilter, EhrContext) {
            return {
                restrict: "EA",
                templateUrl: "thinkehr/f4/templates/ehr-date.html",
                scope: true,
                controller: function ($scope) {
                    $scope.suppressLabel = false;

                    $scope.dateName = function () {
                        return "date_" + $scope.model.getSanitizedUniqueId();
                    };

                    $scope.dateValue = function (value) {
                        return $scope.model.dateValue(value);
                    };

                    this._toDate = function () {
                        var dv = $scope.dateValue();
                        if (dv && dv.length > 0) {
                            //return new Date(dv);
                            return dv;
                        } else {
                            //return null;
                            return new Date().toISOString();
                        }
                    };

                    $scope.dateObject = this._toDate();

                    $scope.$watch("dateObject", function (newVal, oldVal) {
                        if (newVal != oldVal) {
                            var strDate;
                            if (!newVal) {
                                strDate = null;
                            } else {
                                strDate = dateFilter(newVal, "yyyy-MM-dd");
                            }

                            if ($scope.dateValue() != strDate) {
                                $scope.dateValue(strDate);
                            }
                        }
                    });

                    $scope.dateOptions = {
                        culture: EhrContext.locale,
                        start: "month",
                        depth: "month"
                    };

                    var ctrl = this;

                    $scope.model.onModelRefreshed(function () {
                        $scope.dateObject = ctrl._toDate();
                    });

                }
            };
        }]);

        module.directive("ehrTime", ["dateFilter", "EhrContext", function (dateFilter, EhrContext) {
            return {
                restrict: "EA",
                templateUrl: "thinkehr/f4/templates/ehr-time.html",
                scope: true,
                controller: function ($scope) {
                    $scope.suppressLabel = false;
                    $scope.interval = 30;
                    $scope.format = undefined;

                    $scope.timeName = function () {
                        return "time_" + $scope.model.getSanitizedUniqueId();
                    };

                    $scope.timeValue = function (value) {
                        return $scope.model.timeValue(value);
                    };

                    this._toDate = function () {
                        var tv = $scope.timeValue();
                        if (tv && tv.length > 0) {
                            var d = new Date();
                            d.setHours(parseInt(tv.substring(0, 2)), parseInt(tv.substring(3, 5)));

                            if (tv.length > 5) {
                                d.setSeconds(parseInt(tv.substring(6, 8)));
                                if (tv.length > 8) {
                                    d.setMilliseconds(parseInt(tv.substring(9, tv.length)));
                                } else {
                                    d.setMilliseconds(0);
                                }
                            } else {
                                d.setSeconds(0);
                                d.setMilliseconds(0);
                            }

                            return d;
                        } else {
                            return null;
                        }
                    };

                    $scope.timeObject = this._toDate();

                    $scope.$watch("timeObject", function (newVal, oldVal) {
                        if (newVal != oldVal) {
                            var strTime;
                            if (!newVal) {
                                strTime = null;
                            } else {
                                strTime = dateFilter(newVal, "HH:mm:ss.sss");
                            }

                            if ($scope.timeValue() != strTime) {
                                $scope.timeValue(strTime);
                            }
                        }
                    });

                    $scope.timeOptions = {
                        culture: EhrContext.locale,
                        format: $scope.format,
                        parseFormats: ["HH:mm", "HH:mm:ss", "HH:mm:ss.sss"],
                        interval: $scope.interval
                    };

                    if (!$scope.format) {
                        delete $scope.timeOptions.format;
                    }

                    var ctrl = this;

                    $scope.model.onModelRefreshed(function () {
                        $scope.timeObject = ctrl._toDate();
                    });

                }
            };
        }]);

        module.directive("ehrDateTime", ["dateFilter", "EhrContext", function (dateFilter, EhrContext) {
            return {
                restrict: "EA",
                templateUrl: "thinkehr/f4/templates/ehr-date-time.html",
                scope: true,
                controller: function ($scope) {
                    $scope.suppressLabel = false;
                    $scope.interval = 30;
                    $scope.timeFormat = undefined;

                    $scope.dateTimeName = function () {
                        return "date_time_" + $scope.model.getSanitizedUniqueId();
                    };

                    $scope.dateTimeValue = function (value) {
                        return $scope.model.dateTimeValue(value);
                    };

                    this._toDate = function () {
                        var dtv = $scope.dateTimeValue();
                        if (dtv && dtv.length > 0) {
                            return new Date(dtv);
                            //return dtv;
                        } else {
                            return new Date();
                        }
                    };

                    $scope.dateTimeObject = this._toDate();

                    $scope.$watch("dateTimeObject", function (newVal, oldVal) {
                        if (newVal != oldVal) {
                            var strDateTime;
                            if (!newVal) {
                                strDateTime = null;
                            } else {
                                strDateTime = dateFilter(newVal, "yyyy-MM-ddTHH:mm:ss.sssZ");
                            }

                            if ($scope.dateTimeValue() != strDateTime) {
                                $scope.dateTimeValue(strDateTime);
                            }
                        }
                    });

                    $scope.dateTimeOptions = {
                        culture: EhrContext.locale,
                        timeFormat: $scope.timeFormat,
                        interval: $scope.interval,
                        start: "month",
                        depth: "month"
                    };

                    if (!$scope.format) {
                        delete $scope.dateTimeOptions.timeFormat;
                    }

                    var ctrl = this;

                    $scope.model.onModelRefreshed(function () {
                        $scope.dateTimeObject = ctrl._toDate();
                    });

                }
            };
        }]);

        return module;
    }


    // Fixed bootstrap
    createFormModule("fixed-angular-form", "0.0.0", null, null, "thinkehrForms4");

    function bootstrapModule(module, el) {
        angular.element(document).ready(function () {
            angular.bootstrap(el, [module.name]);
            console.info("Bootstrapped angular form module", module.name, ".");
        });

        return module;
    }

    function getAngularDirective(model) {
        if (model.isContainer()) {
            return "thinkehr/f4/templates/ehr-container.html";
        }

        return "thinkehr/f4/templates/ehr-unknown.html";
    }

    // Exports
    thinkehr.f4.ng.createFormModule = createFormModule;
    thinkehr.f4.ng.bootstrapModule = bootstrapModule;
    thinkehr.f4.ng.getDirective = getAngularDirective;
})();
