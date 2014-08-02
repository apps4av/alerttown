<?php

$host="127.0.0.1";
$port=3306;
$socket="";
$user="root";
$password="root";
$dbname="main";

// limit search by time
$hours = $_POST['hours'];
$min = $_POST['minutes'];
if(!isset($hours)) {
        $hours=24;
}
if(!isset($min)) {
        $min=0;
}

$key = $_POST['key'];
if(0 != strcmp($key, 'dc4bc02c-a9f2-11e3-95c7-c328de82a3ed')) {
	die ('Auth failure');
}


$con = new mysqli($host, $user, $password, $dbname, $port, $socket)
or die ('Could not connect to the database server' . mysqli_connect_error());


$query = "SELECT  Event_Name, Location_Lat, Location_Lon, Reported_Time, Event_Reporting_Duration, Event_Reporting_Radius, Reported_count, Reported_Event_ID, Last_Updated,Push_Notification FROM main.Active_Events_View WHERE Last_Updated >= UTC_TIMESTAMP() - INTERVAL " . $hours . " HOUR" . " - INTERVAL " . $min . " MINUTE";

if ($stmt = $con->prepare($query)) {
    $stmt->execute();
    $stmt->bind_result($Event_Name, $Location_Lat, $Location_Lon, $Reported_Time, $Event_Reporting_Duration, $Event_Reporting_Radius, $Reported_count, $Reported_Event_ID, $Last_Updated, $Push_Notification);
    while ($stmt->fetch()) {
        printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n", $Event_Name, $Location_Lat, $Location_Lon, $Reported_Time, $Event_Reporting_Duration, $Event_Reporting_Radius, $Reported_count, $Reported_Event_ID, $Last_Updated, $Push_Notification);
    }
    $stmt->close();
}

?>
