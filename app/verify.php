<?php
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

$host = 'aperichiyk526.mysql.db'; // Adresse du serveur MySQL
$db = 'aperichiyk526'; // Nom de la base
$user = 'aperichiyk526'; // Utilisateur MySQL
$password = 'Cycy100788'; // Mot de passe MySQL

// Vérifier si le code est fourni
if (!isset($_GET['code'])) {
    echo "Code de vérification manquant.";
    exit;
}

$verificationCode = $_GET['code'];

try {
    // Connexion à la base de données
    $pdo = new PDO("mysql:host=$host;dbname=$db", $user, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    // Vérifier si le code existe
    $stmt = $pdo->prepare("SELECT * FROM users WHERE verification_code = ?");
    $stmt->execute([$verificationCode]);
    $user = $stmt->fetch();

    if ($user) {
        if ($user['is_verified']) {
            echo "Votre email est déjà confirmé.";
        } else {
            // Mettre à jour le statut de vérification
            $updateStmt = $pdo->prepare("UPDATE users SET is_verified = 1 WHERE verification_code = ?");
            $updateStmt->execute([$verificationCode]);
            echo "Votre email a été confirmé avec succès !";
        }
    } else {
        echo "Code de vérification invalide.";
    }
} catch (PDOException $e) {
    echo "Erreur : " . $e->getMessage();
}
?>
