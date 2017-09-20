var SyringeProgressTaskTypeFilter = function (TaskTypeEnum)
{
  return function (taskType)
  {
    if(taskType === TaskTypeEnum.PERFUSION_SYRINGE_COMPLETE){
      return 'in-progress';
    }
    else if (taskType === TaskTypeEnum.PERFUSION_SYRINGE_DISPENSE){
      return 'completed';
    }
    return '';
  };
};