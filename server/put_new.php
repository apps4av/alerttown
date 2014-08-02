<?php

$host="127.0.0.1";
$port=3306;
$socket="";
$user="root";
$password="root";
$dbname="main";

$type = mysql_escape_string($_POST["type"]);
$username = mysql_escape_string($_POST["username"]);
$reported = mysql_escape_string($_POST["reported"]);
$lat = mysql_escape_string($_POST["lat"]);
$lon = mysql_escape_string($_POST["lon"]);
$notes = mysql_escape_string($_POST["notes"]);
$uuid = mysql_escape_string($_POST["uuid"]);


$key = $_POST['key'];
if(0 != strcmp($key, 'dc4bc02c-a9f2-11e3-95c7-c328de82a3ed')) {
        die ('Auth failure');
}


$con = new mysqli($host, $user, $password, $dbname, $port, $socket)
or die ('Could not connect to the database server' . mysqli_connect_error());

//$con->close();

$query = "INSERT INTO `main`.`Event_Data` (`Event_Name`, `Reported_BY`, `Reported_Time`, `Location_Lat`, `Location_Lon`, `Event_Notes`, `UUID`) VALUES (" .
	"'" . $type . "'," .
	"'" . $username . "'," .
	"'" . $reported . "'," .
	"'" . $lat . "'," .
	"'" . $lon . "'," .
	"'" . $notes . "'," .
	"'" . $uuid . "'" .
	")";


if ($stmt = $con->prepare($query)) {
    $stmt->execute();
    $stmt->bind_result($field1, $field2);
    while ($stmt->fetch()) {
        //printf("%s, %s\n", $field1, $field2);
    }
    $stmt->close();
}

?>


