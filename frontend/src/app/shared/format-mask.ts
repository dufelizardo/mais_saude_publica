export function formatCpf(raw: string): string {
  const digits = raw.replace(/\D/g, '').slice(0, 11);

  let out = digits.slice(0, 3);
  if (digits.length > 3) out += '.' + digits.slice(3, 6);
  if (digits.length > 6) out += '.' + digits.slice(6, 9);
  if (digits.length > 9) out += '-' + digits.slice(9, 11);
  return out;
}

export function formatCartaoSus(raw: string): string {
  const digits = raw.replace(/\D/g, '').slice(0, 15);

  let out = digits.slice(0, 3);
  if (digits.length > 3) out += ' ' + digits.slice(3, 7);
  if (digits.length > 7) out += ' ' + digits.slice(7, 11);
  if (digits.length > 11) out += ' ' + digits.slice(11, 15);
  return out;
}

export function formatTelefone(raw: string): string {
  // O próprio texto formatado por esta função começa com "+55", que contém dígitos "5" "5" —
  // como o handler de (input) reprocessa o valor já formatado a cada tecla, sem remover esse
  // prefixo antes o `\D` abaixo capturaria os dígitos do prefixo como se fossem o DDD digitado.
  const semPrefixo = raw.startsWith('+55') ? raw.slice(3) : raw;
  const digits = semPrefixo.replace(/\D/g, '').slice(0, 11);
  if (digits.length === 0) return '';

  const ddd = digits.slice(0, 2);
  const resto = digits.slice(2);
  const isCelular = resto.startsWith('9');
  const maxResto = isCelular ? 9 : 8;
  const restoCapped = resto.slice(0, maxResto);

  let out = '+55';
  out += ` (${ddd}`;
  if (digits.length >= 2) out += ')';

  if (restoCapped) {
    const tamanhoMeio = isCelular ? 5 : 4;
    const meio = restoCapped.slice(0, tamanhoMeio);
    const fim = restoCapped.slice(tamanhoMeio);
    out += ` ${meio}`;
    if (fim) out += `-${fim}`;
  }

  return out;
}
