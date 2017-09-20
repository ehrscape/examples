/**
 * @author Matej Poklukar
 */
/**
 * Filter which returns full hour of given date
 * @param  dateString
 * @returns {string} hour in format HH:00
 */
function TimeHourFormatFilter ()
{
  return function (dateString)
  {
    var dateTime = new Date(dateString);
    return dateTime.getHours() + ':00';
  };
}
TimeHourFormatFilter.$inject = [];