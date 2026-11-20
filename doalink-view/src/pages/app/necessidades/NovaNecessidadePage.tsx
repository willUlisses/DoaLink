import { zodResolver } from '@hookform/resolvers/zod'
import { Plus, Trash2 } from 'lucide-react'
import { useState } from 'react'
import { useFieldArray, useForm } from 'react-hook-form'
import { useNavigate } from 'react-router-dom'
import { InfoBanner } from '../../../components/feedback/InfoBanner.tsx'
import { Breadcrumbs } from '../../../components/layout/Breadcrumbs.tsx'
import { AddressFieldset } from '../../../components/ui/AddressFieldset.tsx'
import { Button } from '../../../components/ui/Button.tsx'
import { Card } from '../../../components/ui/Card.tsx'
import { Input } from '../../../components/ui/Input.tsx'
import { Select } from '../../../components/ui/Select.tsx'
import { Textarea } from '../../../components/ui/Textarea.tsx'
import { useDocumentTitle } from '../../../hooks/useDocumentTitle.ts'
import { useToast } from '../../../hooks/useToast.ts'
import {
  criarNecessidadeSchema,
  type CriarNecessidadeFormValues,
} from '../../../schemas/necessidade.schema.ts'
import { criarNecessidade } from '../../../services/necessidades.service.ts'
import { TIPO_ITEM_DOACAO, TIPO_UNIDADE } from '../../../types/enums.ts'
import { applyApiErrorsToForm } from '../../../utils/formErrors.ts'
import { LABELS_CATEGORIA_ITEM, LABELS_UNIDADE } from '../../../utils/labels.ts'
import { limparDigitos } from '../../../utils/validators.ts'

const CATEGORIA_OPTIONS = Object.values(TIPO_ITEM_DOACAO).map((c) => ({
  value: c,
  label: LABELS_CATEGORIA_ITEM[c],
}))

const UNIDADE_OPTIONS = Object.values(TIPO_UNIDADE).map((u) => ({
  value: u,
  label: LABELS_UNIDADE[u],
}))

export function NovaNecessidadePage() {
  useDocumentTitle('Registrar necessidade')
  const navigate = useNavigate()
  const { showToast } = useToast()
  const [formError, setFormError] = useState<string | null>(null)

  const {
    register,
    control,
    handleSubmit,
    setValue,
    setError,
    setFocus,
    formState: { errors, isSubmitting },
  } = useForm<CriarNecessidadeFormValues>({
    resolver: zodResolver(criarNecessidadeSchema),
    defaultValues: {
      itens: [
        {
          tipoItemDoacao: 'NAO_PERECIVEL',
          tipoUnidade: 'KG',
          quantidade: 1,
          descricao: '',
        },
      ],
      localEntrega: {
        cep: '',
        estado: '',
        cidade: '',
        bairro: '',
        logradouro: '',
        numero: '',
        complemento: '',
      },
    },
  })

  const { fields, append, remove } = useFieldArray({
    control,
    name: 'itens',
  })

  const onSubmit = async (values: CriarNecessidadeFormValues) => {
    setFormError(null)
    try {
      const criada = await criarNecessidade({
        itens: values.itens.map((item) => ({
          tipoItemDoacao: item.tipoItemDoacao,
          tipoUnidade: item.tipoUnidade,
          quantidade: item.quantidade,
          descricao: item.descricao?.trim() || undefined,
        })),
        localEntrega: {
          cep: limparDigitos(values.localEntrega.cep),
          estado: values.localEntrega.estado.trim().toUpperCase(),
          cidade: values.localEntrega.cidade.trim(),
          bairro: values.localEntrega.bairro.trim(),
          logradouro: values.localEntrega.logradouro.trim(),
          numero: values.localEntrega.numero?.trim() || undefined,
          complemento: values.localEntrega.complemento?.trim() || undefined,
        },
      })

      showToast({
        variant: 'success',
        title: `Necessidade #${criada.id} registrada`,
        description: 'A necessidade foi publicada com situação ABERTA.',
      })
      navigate('/app/minhas-necessidades')
    } catch (error) {
      const msg = applyApiErrorsToForm(error, setError, setFocus)
      setFormError(msg)
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <Breadcrumbs
          items={[
            { label: 'Painel', to: '/app' },
            { label: 'Minhas necessidades', to: '/app/minhas-necessidades' },
            { label: 'Nova necessidade' },
          ]}
        />
        <h1 className="text-2xl font-bold text-ink">Registrar necessidade de doação</h1>
        <p className="mt-1 text-sm text-ink-secondary">
          Informe os itens necessários e o endereço completo de entrega.
        </p>
      </div>

      {formError ? <InfoBanner tone="danger">{formError}</InfoBanner> : null}

      <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-6">
        <Card className="space-y-4">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <h2 className="text-base font-bold text-ink">Itens necessários</h2>
            <Button
              type="button"
              variant="secondary"
              size="sm"
              disabled={fields.length >= 50}
              leftIcon={<Plus className="h-4 w-4" />}
              onClick={() =>
                append({
                  tipoItemDoacao: 'NAO_PERECIVEL',
                  tipoUnidade: 'KG',
                  quantidade: 1,
                  descricao: '',
                })
              }
            >
              Adicionar item
            </Button>
          </div>

          <div className="space-y-4">
            {fields.map((field, index) => (
              <div
                key={field.id}
                className="rounded-[12px] border border-border bg-canvas p-4 space-y-3"
              >
                <div className="flex items-center justify-between gap-2">
                  <span className="text-xs font-bold uppercase tracking-wide text-ink-secondary">
                    Item #{index + 1}
                  </span>
                  {fields.length > 1 ? (
                    <Button
                      type="button"
                      variant="ghost"
                      size="sm"
                      onClick={() => remove(index)}
                      leftIcon={<Trash2 className="h-3.5 w-3.5 text-danger" />}
                      className="text-danger hover:bg-danger-soft"
                    >
                      Remover
                    </Button>
                  ) : null}
                </div>

                <div className="grid grid-cols-1 gap-3 sm:grid-cols-12">
                  <div className="sm:col-span-4">
                    <Select
                      label="Categoria"
                      options={CATEGORIA_OPTIONS}
                      required
                      error={errors.itens?.[index]?.tipoItemDoacao?.message}
                      {...register(`itens.${index}.tipoItemDoacao`)}
                    />
                  </div>
                  <div className="sm:col-span-4">
                    <Select
                      label="Unidade"
                      options={UNIDADE_OPTIONS}
                      required
                      error={errors.itens?.[index]?.tipoUnidade?.message}
                      {...register(`itens.${index}.tipoUnidade`)}
                    />
                  </div>
                  <div className="sm:col-span-4">
                    <Input
                      label="Quantidade"
                      type="number"
                      step="any"
                      min="0.01"
                      required
                      error={errors.itens?.[index]?.quantidade?.message}
                      {...register(`itens.${index}.quantidade`, { valueAsNumber: true })}
                    />
                  </div>
                  <div className="sm:col-span-12">
                    <Textarea
                      label="Descrição complementar (opcional)"
                      placeholder="Especificações, restrições ou detalhes do item (até 500 caracteres)"
                      maxLength={500}
                      rows={2}
                      error={errors.itens?.[index]?.descricao?.message}
                      {...register(`itens.${index}.descricao`)}
                    />
                  </div>
                </div>
              </div>
            ))}
          </div>
        </Card>

        <Card>
          <AddressFieldset
            prefix="localEntrega"
            legend="Local de entrega"
            description="Endereço físico obrigatório onde as doações devem ser entregues."
            register={register}
            setValue={setValue}
            errors={errors.localEntrega}
            required
          />
        </Card>

        <div className="flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
          <Button
            type="button"
            variant="secondary"
            onClick={() => navigate('/app/minhas-necessidades')}
          >
            Cancelar
          </Button>
          <Button
            type="submit"
            variant="receptor"
            size="lg"
            loading={isSubmitting}
            loadingLabel="Registrando..."
          >
            Registrar necessidade
          </Button>
        </div>
      </form>
    </div>
  )
}

