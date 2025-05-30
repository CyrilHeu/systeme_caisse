<?php
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

$host = 'aperichiyk526.mysql.db'; // Adresse du serveur MySQL
$db = 'aperichiyk526'; // Nom de la base
$user = 'aperichiyk526'; // Utilisateur MySQL
$password = 'Cycy100788'; // Mot de passe MySQL

// Vérifier si l'email est fourni
if (!isset($_POST['email'])) {
    echo json_encode(['status' => 'error', 'message' => 'Email manquant']);
    exit;
}

$email = $_POST['email'];

try {
    // Connexion à la base de données
    $pdo = new PDO("mysql:host=$host;dbname=$db", $user, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    // Vérifier si l'utilisateur existe et est vérifié
    $stmt = $pdo->prepare("SELECT is_verified FROM users WHERE email = ?");
    $stmt->execute([$email]);
    $user = $stmt->fetch();

    if ($user) {
        // Retourner le statut de vérification
        echo json_encode(['status' => 'success', 'is_verified' => (bool)$user['is_verified']]);
    } else {
        // L'utilisateur n'existe pas
        echo json_encode(['status' => 'error', 'message' => 'Utilisateur introuvable']);
    }
} catch (PDOException $e) {
    // Gérer les erreurs de connexion ou de requête
    echo json_encode(['status' => 'error', 'message' => $e->getMessage()]);
}
?>