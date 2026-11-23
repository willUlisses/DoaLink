import type {
  FieldErrors,
  FieldValues,
  Path,
  PathValue,
  UseFormRegister,
  UseFormSetValue,
} from 'react-hook-form'
import { UFS_BRASIL } from '../../types/enums.ts'
import { mascararCep } from '../../utils/masks.ts'
import { Input } from './Input.tsx'
import { Select } from './Select.tsx'

export interface AddressFieldValue {
  cep: string
  estado: string
  cidade: string
  bairro: string
  logradouro: string
  numero?: string
  complemento?: string
}

interface AddressFieldsetProps<TFieldValues extends FieldValues> {
  prefix: Path<TFieldValues>
  legend: string
  description?: string
  register: UseFormRegister<TFieldValues>
  setValue: UseFormSetValue<TFieldValues>
  errors?: FieldErrors<AddressFieldValue>
  required?: boolean
  compact?: boolean
}

const UF_OPTIONS = UFS_BRASIL.map((uf) => ({ value: uf, label: uf }))

export function AddressFieldset<TFieldValues extends FieldValues>({
  prefix,
  legend,
  description,
  register,
  setValue,
  errors,
  required = true,
  compact = false,
}: AddressFieldsetProps<TFieldValues>) {
  const fieldPath = (name: keyof AddressFieldValue): Path<TFieldValues> =>
    `${prefix}.${name}` as Path<TFieldValues>

  const cepField = register(fieldPath('cep'))

  return (
    <fieldset
      className={`rounded-card border border-border bg-surface-subtle/50 ${
        compact ? 'p-3.5' : 'p-4 sm:p-5'
      }`}
    >
      <legend className="px-1.5 text-sm font-semibold text-ink">{legend}</legend>
      {description ? (
        <p className={`${compact ? 'mb-2.5' : 'mb-4'} text-xs text-ink-secondary`}>{description}</p>
      ) : null}

      <div className={`grid grid-cols-1 sm:grid-cols-6 ${compact ? 'gap-2.5' : 'gap-4'}`}>
        <div className="sm:col-span-2">
          <Input
            label="CEP"
            placeholder="00000-000"
            inputMode="numeric"
            maxLength={9}
            required={required}
            compact={compact}
            error={errors?.cep?.message}
            {...cepField}
            onChange={(event) => {
              const masked = mascararCep(event.target.value)
              setValue(
                fieldPath('cep'),
                masked as PathValue<TFieldValues, Path<TFieldValues>>,
                { shouldValidate: Boolean(errors?.cep) },
              )
            }}
          />
        </div>

        <div className="sm:col-span-2">
          <Select
            label="UF"
            placeholder="Selecione"
            options={UF_OPTIONS}
            required={required}
            compact={compact}
            error={errors?.estado?.message}
            {...register(fieldPath('estado'))}
          />
        </div>

        <div className="sm:col-span-2">
          <Input
            label="Cidade"
            placeholder="Ex.: São Paulo"
            maxLength={255}
            required={required}
            compact={compact}
            error={errors?.cidade?.message}
            {...register(fieldPath('cidade'))}
          />
        </div>

        <div className="sm:col-span-3">
          <Input
            label="Bairro"
            placeholder="Ex.: Centro"
            maxLength={255}
            required={required}
            compact={compact}
            error={errors?.bairro?.message}
            {...register(fieldPath('bairro'))}
          />
        </div>

        <div className="sm:col-span-3">
          <Input
            label="Logradouro"
            placeholder="Rua, avenida, praça..."
            maxLength={255}
            required={required}
            compact={compact}
            error={errors?.logradouro?.message}
            {...register(fieldPath('logradouro'))}
          />
        </div>

        <div className="sm:col-span-2">
          <Input
            label="Número"
            placeholder="S/N se vazio"
            maxLength={20}
            compact={compact}
            hint={compact ? undefined : 'Se vazio, será salvo como S/N.'}
            error={errors?.numero?.message}
            {...register(fieldPath('numero'))}
          />
        </div>

        <div className="sm:col-span-4">
          <Input
            label="Complemento"
            placeholder="Sala, galpão, ponto de referência (opcional)"
            maxLength={100}
            compact={compact}
            error={errors?.complemento?.message}
            {...register(fieldPath('complemento'))}
          />
        </div>
      </div>
    </fieldset>
  )
}

