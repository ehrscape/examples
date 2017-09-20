Class.define('app.views.medications.common.DosingPatternValidator', 'tm.jquery.Object', {
  view: null,

  /**
   * @param {Array<String>} times
   * @returns {Boolean}
   */
  isIncreasingSequenceValid: function(times)
  {
    var minutesSum = 0;
    for (var i = 0; i < times.length; i++)
    {
      var previousTime = i > 0 && times[i - 1] !== null ? times[i - 1] : null;
      var previousTimeInMinutes = previousTime !== null ? previousTime.getHours() * 60 + previousTime.getMinutes() : null;
      var time = times[i];

      if (time === null)
      {
        continue;
      }
      else if (previousTimeInMinutes !== null)
      {
        var timeInMinutes = time.getHours() * 60 + time.getMinutes();
        var sameDay = timeInMinutes > previousTimeInMinutes;
        if (sameDay)
        {
          minutesSum += timeInMinutes - previousTimeInMinutes;
        }
        else
        {
          minutesSum += 24 * 60 - previousTimeInMinutes + timeInMinutes;
        }
      }
    }
    if (minutesSum >= 24 * 60)
    {
      return false;
    }
    return true;
  },

  /**
   * @param {Array<String>} times
   * @returns {Boolean}
   */
  isFieldValueExistent: function(times)
  {
    for (var i = 0; i < times.length; i++)
    {
      if (times[i] === null) return false;
    }
    return true;
  },

  /**
   * @param {tm.jquery.Component|*} component
   * @param {Array<String>} times
   * @returns {tm.jquery.FormField}
   */
  getDosingPatternValidation: function(component, times)
  {
    var self = this;
    return new tm.jquery.FormField({
      component: component,
      required: false,
      componentValueImplementationFn: function()
      {
        return times;
      },
      validation: {
        type: "local",
        validators: [
          new tm.jquery.Validator({
            errorMessage: this.getView().getDictionary("dosing.pattern.increasing.sequence.required"),
            isValid: function(times)
            {
              return self.isIncreasingSequenceValid(times);
            }
          }),
          new tm.jquery.RequiredValidator({
            errorMessage: this.getView().getDictionary("all.values.required"),
            isValid: function(times)
            {
              return self.isFieldValueExistent(times);
            }
          })
        ]
      }
    });
  },

  /**
   * @returns {app.views.common.AppView}
   */
  getView: function()
  {
    return this.view;
  }
});