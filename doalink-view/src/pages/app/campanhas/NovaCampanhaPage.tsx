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
import { useReceptorAddressPrefill } from '../../../hooks/useReceptorAddressPrefill.ts'
import { useToast } from '../../../hooks/useToast.ts'
import {
  criarCampanhaSchema,
  type CriarCampanhaFormValues,
} from '../../../schemas/campanha.schema.ts'
import { criarCampanha } from '../../../services/campanhas.service.ts'
import { TIPO_ITEM_DOACAO, TIPO_UNIDADE } from '../../../types/enums.ts'
import { applyApiErrorsToForm } from '../../../utils/formErrors.ts'
import {
  LABELS_CATEGORIA_ITEM,
  LABELS_SITUACAO_CAMPANHA,
  LABELS_UNIDADE,
} from '../../../utils/labels.ts'
import { limparDigitos } from '../../../utils/validators.ts'

const CATEGORIA_OPTIONS = Object.values(TIPO_ITEM_DOACAO).map((c) => ({
  value: c,
  label: LABELS_CATEGORIA_ITEM[c],
}))

const UNIDADE_OPTIONS = Object.values(TIPO_UNIDADE).map((u) => ({
  value: u,
  label: LABELS_UNIDADE[u],
}))

export function NovaCampanhaPage() {
  useDocumentTitle('Nova campanha emergencial')
  const navigate = useNavigate()
  const { showToast } = useToast()
  const [formError, setFormError] = useState<string | null>(null)

  const {
    register,
    control,
    handleSubmit,
    getValues,
    setValue,
    setError,
    setFocus,
    formState: { errors, isSubmitting },
  } = useForm<CriarCampanhaFormValues>({
    resolver: zodResolver(criarCampanhaSchema),
    defaultValues: {
      nome: '',
      descricao: '',
      enderecoRecebimento: {
        cep: '',
        estado: '',
        cidade: '',
        bairro: '',
        logradouro: '',
        numero: '',
        complemento: '',
      },
      itens: [],
    },
  })

  const { fields, append, remove } = useFieldArray({
    control,
    name: 'itens',
  })

  useReceptorAddressPrefill('enderecoRecebimento', setValue, getValues)

  const onSubmit = async (values: CriarCampanhaFormValues) => {
    setFormError(null)
    try {
      const criada = await criarCampanha({
        nome: values.nome.trim(),
        descricao: values.descricao?.trim() || undefined,
        enderecoRecebimento: {
          cep: limparDigitos(values.enderecoRecebimento.cep),
          estado: values.enderecoRecebimento.estado.trim().toUpperCase(),
          cidade: values.enderecoRecebimento.cidade.trim(),
          bairro: values.enderecoRecebimento.bairro.trim(),
          logradouro: values.enderecoRecebimento.logradouro.trim(),
          numero: values.enderecoRecebimento.numero?.trim() || undefined,
          complemento: values.enderecoRecebimento.complemento?.trim() || undefined,
        },
        itens:
          values.itens.length > 0
            ? values.itens.map((item) => ({
                tipoItemDoacao: item.tipoItemDoacao,
                tipoUnidade: item.tipoUnidade,
                quantidade: item.quantidade,
                descricao: item.descricao?.trim() || undefined,
              }))
            : undefined,
      })

      showToast({
        variant: 'success',
        title: `Campanha "${criada.nome}" criada`,
        description: `A campanha já está publicada com situação "${LABELS_SITUACAO_CAMPANHA.ATIVA}".`,
      })
      navigate('/app/minhas-campanhas')
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
            { label: 'Minhas campanhas', to: '/app/minhas-campanhas' },
            { label: 'Nova campanha' },
          ]}
        />
        <h1 className="text-2xl font-bold text-ink">Criar campanha emergencial</h1>
        <p className="mt-1 text-sm text-ink-secondary">
          A campanha inicia com situação {LABELS_SITUACAO_CAMPANHA.ATIVA} e exibe o ponto físico de
          recebimento das doações.
        </p>
      </div>

      {formError ? <InfoBanner tone="danger">{formError}</InfoBanner> : null}

      <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-6">
        <Card className="space-y-4">
          <Input
            label="Nome da campanha"
            placeholder="Ex.: Apoio emergencial às famílias do Vale"
            maxLength={150}
            required
            error={errors.nome?.message}
            {...register('nome')}
          />

          <Textarea
            label="Descrição da causa (opcional)"
            placeholder="Descreva a situação emergencial, público atendido e orientações de entrega (até 1000 caracteres)"
            maxLength={1000}
            rows={4}
            error={errors.descricao?.message}
            {...register('descricao')}
          />
        </Card>

        <Card>
          <AddressFieldset
            prefix="enderecoRecebimento"
            legend="Endereço de recebimento"
            description="Local onde as doações desta campanha serão recebidas."
            register={register}
            setValue={setValue}
            errors={errors.enderecoRecebimento}
            required
          />
        </Card>

        <Card className="space-y-4">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <h2 className="text-base font-bold text-ink">
                Necessidades emergenciais (opcional)
              </h2>
              <p className="text-xs text-ink-secondary">
                Liste os itens prioritários para esta campanha, se desejar.
              </p>
            </div>
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

          {fields.length === 0 ? (
            <p className="rounded-button border border-dashed border-border p-4 text-center text-xs text-ink-secondary">
              Nenhum item adicionado ainda. Clique em "Adicionar item" caso queira detalhar
              quantidades.
            </p>
          ) : (
            <div className="space-y-4">
              {fields.map((field, index) => (
                <div
                  key={field.id}
                  className="rounded-card border border-border bg-canvas p-4 space-y-3"
                >
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-bold uppercase tracking-wide text-ink-secondary">
                      Item #{index + 1}
                    </span>
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
                      <Input
                        label="Descrição do item (opcional)"
                        placeholder="Detalhes do item necessário"
                        maxLength={500}
                        error={errors.itens?.[index]?.descricao?.message}
                        {...register(`itens.${index}.descricao`)}
                      />
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </Card>

        <div className="flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
          <Button
            type="button"
            variant="secondary"
            onClick={() => navigate('/app/minhas-campanhas')}
          >
            Cancelar
          </Button>
          <Button
            type="submit"
            variant="receptor"
            size="lg"
            loading={isSubmitting}
            loadingLabel="Publicando campanha..."
          >
            Publicar campanha
          </Button>
        </div>
      </form>
    </div>
  )
}

