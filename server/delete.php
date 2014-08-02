<?php

$host="127.0.0.1";
$port=3306;
$socket="";
$user="root";
$password="root";
$dbname="main";

$id = mysql_escape_string($_POST["id"]);

$key = $_POST['key'];
if(0 != strcmp($key, 'dc4bc02c-a9f2-11e3-95c7-c328de82a3ed')) {
        die ('Auth failure');
}


$con = new mysqli($host, $user, $password, $dbname, $port, $socket)
or die ('Could not connect to the database server' . mysqli_connect_error());

//$con->close();

$query = "DELETE FROM `main`.`Event_Data` WHERE Reported_Event_ID=" .
	"'" . $id . "'";

if ($stmt = $con->prepare($query)) {
    $stmt->execute();
    $stmt->close();
}

?>


