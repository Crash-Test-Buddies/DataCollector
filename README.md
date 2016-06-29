# Data Collector

## Usage
First register the service
Populate a DBParcelable object and send as a Parcelable with an intent

## Result
Adds a row to a step table with timings for a particular step. 
## Example
<service android:name=".service.DBHandlerService" android:exported="true"/>
    
Intent mServiceIntent = new Intent(getActivity(), DBHandlerService.class);
mServiceIntent.putExtra(DBHandlerService.PARCELABLE_NAME, dbParcelable);
// Starts the IntentService
startService(mServiceIntent);