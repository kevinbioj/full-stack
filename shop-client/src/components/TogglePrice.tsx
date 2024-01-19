import { ToggleButton, ToggleButtonGroup } from '@mui/material';
import { Devise } from '../types/locale';
import React from 'react';

type TogglePriceProps = {
    value: Devise;
    onChange: (value: Devise) => void;
};

const TogglePrice = ({ value, onChange }: TogglePriceProps) => {
    const values = Object.keys(Devise);

    return (
        <ToggleButtonGroup
            exclusive
            color="primary"
            size="small"
            aria-label="Devise"
            value={value}
            onChange={(_, value) => onChange(value)}
        >
            {values.map((value, i) => (
                <ToggleButton value={value} key={i}>
                    {value}
                </ToggleButton>
            ))}
        </ToggleButtonGroup>
    );
};

export default TogglePrice;
