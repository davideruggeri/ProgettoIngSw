$word = New-Object -ComObject Word.Application
$word.Visible = $false
$doc = $word.Documents.Open("C:\App\ProgettoIngSw\Elaborato2025_26_def.pdf", $false, $true)
$text = $doc.Content.Text
Set-Content -Path "C:\App\ProgettoIngSw\pdf_text.txt" -Value $text
$doc.Close([ref]$false)
$word.Quit()
[System.Runtime.InteropServices.Marshal]::ReleaseComObject($word) | Out-Null
