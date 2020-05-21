// IPositionRecordService.aidl
package mcteam08.assign.assign02;

// Declare any non-default types here with import statements

interface IPositionRecordService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    double getLongitude();
    double getLatitude();
    double getDistance();
    double getAverageSpeed();
}
