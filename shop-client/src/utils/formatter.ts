import { Devise } from '../types/locale';

/**
 *
 * @param word The word to pluralize if necessary
 * @param nb The number which preceeds the word
 * @returns The word pluralized if it was necessary
 */
export const pluralize = (word: string, nb: number) => `${word}${nb > 1 ? 's' : ''}`;

/**
 *
 * @param price The price to formatter
 * @returns The formatted price
 */
export const priceFormatter = (priceEUR: number, priceUSD: number, devise: Devise) => {
    const formatter = new Intl.NumberFormat(devise == Devise.EUR ? 'fr-FR' : 'en-US', {
        style: 'currency',
        currency: devise,
    });
    return devise == Devise.EUR ? formatter.format(priceEUR) : formatter.format(priceUSD);
};
